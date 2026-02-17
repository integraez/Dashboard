package com.integrationhub.dashboard.service;

import com.integrationhub.dashboard.config.TibcoEmsProperties;
import com.integrationhub.dashboard.model.QueueInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TibcoEmsQueueService {

    private static final Logger logger = LoggerFactory.getLogger(TibcoEmsQueueService.class);
    private static final int TIMEOUT_SECONDS = 2; // Reduced from 5 for faster response
    private static final long HIGH_VOLUME_THRESHOLD = 3000;

    private final TibcoEmsService tibcoEmsService;
    private final ExecutorService executorService;
    private final boolean tibcoLibAvailable;
    private final Map<String, String> serverStatusMap = new ConcurrentHashMap<>(); // Track server connection status

    public TibcoEmsQueueService(TibcoEmsService tibcoEmsService) {
        this.tibcoEmsService = tibcoEmsService;
        this.executorService = Executors.newFixedThreadPool(10);
        this.tibcoLibAvailable = checkTibcoLibraryAvailable();
        logger.info("TibcoEmsQueueService initialized - TIBCO library available: {}", tibcoLibAvailable);
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            logger.info("Shutting down TibcoEmsQueueService executor service");
            executorService.shutdownNow();
        }
    }

    public List<TibcoEmsProperties.Server> getAllServers() {
        return tibcoEmsService.getAllServers();
    }

    private boolean checkTibcoLibraryAvailable() {
        try {
            Class.forName("com.tibco.tibjms.admin.TibjmsAdmin");
            return true;
        } catch (ClassNotFoundException e) {
            logger.warn("TIBCO EMS library not found. Please add tibjms.jar to the lib/ directory.");
            return false;
        }
    }

    public List<QueueInfo> getHighVolumeQueues() {
        if (!tibcoLibAvailable) {
            logger.info("Using fallback data - TIBCO libraries not available");
            return getMockData();
        }

        List<TibcoEmsProperties.Server> servers = tibcoEmsService.getAllServers();
        List<Future<List<QueueInfo>>> futures = new ArrayList<>();

        // Submit tasks for each server
        for (TibcoEmsProperties.Server server : servers) {
            futures.add(executorService.submit(() -> getQueuesFromServer(server)));
        }

        // Collect results
        List<QueueInfo> allQueues = new ArrayList<>();
        for (Future<List<QueueInfo>> future : futures) {
            try {
                List<QueueInfo> serverQueues = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                allQueues.addAll(serverQueues);
            } catch (TimeoutException e) {
                logger.warn("Timeout getting queues from server");
            } catch (Exception e) {
                logger.error("Error getting queues: {}", e.getMessage());
            }
        }

        // Filter for critical queues (>10000 messages) and warning queues (>5000 messages)
        // Exclude BAM and FCWEB.CACHE queues, sort by server name
        List<QueueInfo> result = allQueues.stream()
                .filter(q -> {
                    boolean isCriticalOrWarning = q.getStatus().equals("critical") || q.getStatus().equals("warning");
                    if (q.getServerName().contains("SC")) {
                        logger.info("  [SC-Spectrum Queue] {}: status={}, count={}, passes filter={}", 
                                   q.getQueueName(), q.getStatus(), q.getMessageCount(), isCriticalOrWarning);
                    }
                    return isCriticalOrWarning;
                })
                .filter(q -> !q.getQueueName().toUpperCase().contains("BAM"))
                .filter(q -> !q.getQueueName().toUpperCase().contains("FCWEB.CACHE"))
                .sorted(Comparator.comparing(QueueInfo::getServerName)
                        .thenComparing(q -> -q.getMessageCount())) // Sort by message count descending
                .limit(50)
                .toList();

        logger.info("Found {} critical/warning queues from {} total queues", result.size(), allQueues.size());
        result.forEach(q -> logger.info("  [{}] {}/{}: {} messages", q.getStatus(), q.getServerName(), q.getQueueName(), q.getMessageCount()));

        // If no real data, fall back to mock
        if (result.isEmpty()) {
            logger.info("No real data found, using mock data");
            return getMockData();
        }
        return result;
    }

    private List<QueueInfo> getMockData() {
        List<QueueInfo> mockQueues = new ArrayList<>();
        mockQueues.add(new QueueInfo("TST1-NEW", "ORDER.PROCESSING.DLQ", 5247));
        mockQueues.add(new QueueInfo("TST2-ESB", "INVENTORY.UPDATE.QUEUE", 4832));
        mockQueues.add(new QueueInfo("TST2-SHORE", "PAYMENT.RETRY.QUEUE", 8915));
        mockQueues.add(new QueueInfo("TST6-ESB", "NOTIFICATION.BACKLOG", 3421));
        mockQueues.add(new QueueInfo("TSTSH1-EMS1-ESB", "BOOKING.SYNC.QUEUE", 12450));
        mockQueues.add(new QueueInfo("TSTSH1-EMS2-ESB", "DATA.MIGRATION.QUEUE", 6789));
        mockQueues.add(new QueueInfo("TSTSH2-EMS1-ESB", "ERROR.HANDLING.QUEUE", 3156));
        mockQueues.add(new QueueInfo("TSTSH3-EMS1-ESB", "AUDIT.LOG.QUEUE", 4521));
        mockQueues.add(new QueueInfo("SC-Spectrum", "SC.Q.AUDIT.PUBLISHMESSAGE.AUDITLOGGER", 14325));
        
        // Filter mock data to only return critical and warning queues
        return mockQueues.stream()
                .filter(q -> q.getStatus().equals("critical") || q.getStatus().equals("warning"))
                .toList();
    }

    private String decryptPassword(String password) {
        // If password starts with #!, it's obfuscated and needs to be decoded
        if (password != null && password.startsWith("#!")) {
            try {
                Class<?> obfuscateClass = Class.forName("com.tibco.tibjms.TibjmsObfuscate");
                Object result = obfuscateClass.getMethod("decode", String.class)
                        .invoke(null, password.substring(2)); // Remove #! prefix
                return (String) result;
            } catch (Exception e) {
                logger.warn("Failed to decrypt password, using as-is: {}", e.getMessage());
                return password;
            }
        }
        return password;
    }

    public List<QueueInfo> getAllQueuesForServer(String serverName) {
        if (!tibcoLibAvailable) {
            logger.info("Using fallback data - TIBCO libraries not available");
            return List.of();
        }

        TibcoEmsProperties.Server server = tibcoEmsService.getAllServers().stream()
                .filter(s -> s.getName().equals(serverName))
                .findFirst()
                .orElse(null);

        if (server == null) {
            logger.warn("Server not found: {}", serverName);
            return List.of();
        }

        List<QueueInfo> queues = getQueuesFromServer(server, false);
        
        // Sort by message count descending (highest first)
        return queues.stream()
                .sorted(Comparator.comparing(QueueInfo::getMessageCount).reversed())
                .toList();
    }

    private List<QueueInfo> getQueuesFromServer(TibcoEmsProperties.Server server) {
        return getQueuesFromServer(server, true);
    }

    private List<QueueInfo> getQueuesFromServer(TibcoEmsProperties.Server server, boolean filterHighVolume) {
        List<QueueInfo> queues = new ArrayList<>();
        Object admin = null;

        try {
            // Use reflection to avoid compile-time dependency
            Class<?> tibjmsAdminClass = Class.forName("com.tibco.tibjms.admin.TibjmsAdmin");
            
            // Build connection URL
            String url = (server.isSslEnabled() ? "ssl://" : "tcp://") + 
                         server.getHost() + ":" + server.getPort();

            // Decrypt password if it's obfuscated
            String decryptedPassword = decryptPassword(server.getPassword());

            logger.info("Attempting connection to {} at {} with user {}", 
                       server.getName(), url, server.getUsername());

            // Connect to TIBCO EMS Admin: new TibjmsAdmin(url, username, password)
            admin = tibjmsAdminClass.getConstructor(String.class, String.class, String.class)
                    .newInstance(url, server.getUsername(), decryptedPassword);

            logger.info("Successfully connected to {}", server.getName());
            serverStatusMap.put(server.getName(), "OK");

            // Get all queues: admin.getQueues()
            Object[] queueInfos = (Object[]) tibjmsAdminClass.getMethod("getQueues").invoke(admin);
            
            if (queueInfos != null) {
                Class<?> queueInfoClass = Class.forName("com.tibco.tibjms.admin.QueueInfo");
                for (Object queueInfo : queueInfos) {
                    try {
                        String queueName = (String) queueInfoClass.getMethod("getName").invoke(queueInfo);
                        long messageCount = (Long) queueInfoClass.getMethod("getPendingMessageCount").invoke(queueInfo);
                        
                        // Skip FCWEB.CACHE queues and filter by volume if needed
                        if (!queueName.toUpperCase().contains("FCWEB.CACHE") && 
                            (!filterHighVolume || messageCount > HIGH_VOLUME_THRESHOLD)) {
                            queues.add(new QueueInfo(
                                server.getName(),
                                queueName,
                                messageCount
                            ));
                        }
                    } catch (Exception e) {
                        logger.debug("Error getting queue details: {}", e.getMessage());
                    }
                }
            }
            
            logger.info("Retrieved {} queues from {}", queues.size(), server.getName());
            
        } catch (ClassNotFoundException e) {
            logger.error("TIBCO library not found: {}", e.getMessage());
            serverStatusMap.put(server.getName(), "UNREACHABLE");
        } catch (Exception e) {
            logger.error("Error connecting to server {}: {} - {}", 
                        server.getName(), e.getClass().getName(), e.getMessage(), e);
            serverStatusMap.put(server.getName(), "UNREACHABLE");
        } finally {
            if (admin != null) {
                try {
                    admin.getClass().getMethod("close").invoke(admin);
                } catch (Exception e) {
                    logger.debug("Error closing admin connection: {}", e.getMessage());
                }
            }
        }

        return queues;
    }

    public Map<String, String> getServerStatus() {
        return new ConcurrentHashMap<>(serverStatusMap);
    }
}
