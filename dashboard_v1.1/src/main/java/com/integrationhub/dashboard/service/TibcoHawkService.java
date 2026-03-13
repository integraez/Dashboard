package com.integrationhub.dashboard.service;

import com.integrationhub.dashboard.config.TibcoHawkProperties;
import com.integrationhub.dashboard.model.HawkApplicationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class TibcoHawkService {

    private static final Logger logger = LoggerFactory.getLogger(TibcoHawkService.class);
    private static final String APP_PREFIX = "Application Name:";
    private static final String SERVICE_PREFIX = "Service Name:";
    private static final String DEPLOYMENT_PREFIX = "Deployment Status:";
    private static final String INSTANCE_PREFIX = "Service Instance Name:";
    private static final String MACHINE_PREFIX = "Machine Name:";
    private static final String STATUS_PREFIX = "Status:";
    private static final String DEFAULT_MOCK_DATA_CLASSPATH = "sample-data/hawk-monitor.sample.txt";

    private final TibcoHawkProperties hawkProperties;
    private volatile String lastLoadError;

    private static final class ParserState {
        String applicationName;
        String serviceName;
        String serviceDeploymentStatus;
        String instanceName;
        String instanceDeploymentStatus;
        String machineName;
        String instanceStatus;

        void clearInstanceContext() {
            instanceName = null;
            instanceDeploymentStatus = null;
            machineName = null;
            instanceStatus = null;
        }

        void clearServiceContext() {
            serviceName = null;
            serviceDeploymentStatus = null;
            clearInstanceContext();
        }
    }

    public TibcoHawkService(TibcoHawkProperties hawkProperties) {
        this.hawkProperties = hawkProperties;
    }

    public List<TibcoHawkProperties.Domain> getAllDomains() {
        Set<String> ships = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (HawkApplicationStatus status : getAllApplicationStatuses()) {
            String ship = safeTrim(status.getDomainName());
            if (!ship.isEmpty()) {
                ships.add(ship);
            }
        }

        if (!ships.isEmpty()) {
            List<TibcoHawkProperties.Domain> derivedDomains = new ArrayList<>();
            for (String ship : ships) {
                TibcoHawkProperties.Domain domain = new TibcoHawkProperties.Domain();
                domain.setName(ship);
                derivedDomains.add(domain);
            }
            return derivedDomains;
        }

        return hawkProperties.getDomains();
    }

    public List<HawkApplicationStatus> getApplicationStatusesForDomain(String domainName) {
        String targetDomain = safeTrim(domainName);
        if (targetDomain.isEmpty()) {
            return List.of();
        }

        return getAllApplicationStatuses().stream()
                .filter(status -> targetDomain.equalsIgnoreCase(safeTrim(status.getDomainName())))
                .toList();
    }

    public List<HawkApplicationStatus> getAllApplicationStatuses() {
        return loadStatusesFromConfiguredSource();
    }

    public Map<String, String> getDomainStatus() {
        List<HawkApplicationStatus> statuses = getAllApplicationStatuses();
        Map<String, String> statusMap = new LinkedHashMap<>();

        if (statuses.isEmpty() && lastLoadError != null) {
            for (TibcoHawkProperties.Domain domain : hawkProperties.getDomains()) {
                statusMap.put(domain.getName(), "FILE_ERROR");
            }
            if (statusMap.isEmpty()) {
                statusMap.put("HAWK_FILE", "FILE_ERROR");
            }
            return statusMap;
        }

        Map<String, Long> countsByShip = statuses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        status -> safeTrim(status.getDomainName()),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));

        for (String ship : countsByShip.keySet()) {
            statusMap.put(ship, "FILE_LOADED");
        }

        if (statusMap.isEmpty()) {
            for (TibcoHawkProperties.Domain domain : hawkProperties.getDomains()) {
                statusMap.put(domain.getName(), "NO_DATA");
            }
        }

        return statusMap;
    }

    private List<HawkApplicationStatus> loadStatusesFromConfiguredSource() {
        String configuredPath = safeTrim(hawkProperties.getDataFile());
        if (configuredPath.isEmpty()) {
            logger.warn("Hawk monitor data file path is not configured. Set tibco.hawk.data-file or TIBCO_HAWK_MONITOR_FILE.");
            return loadMockStatuses("tibco.hawk.data-file is not configured");
        }

        boolean classpathConfigured = configuredPath.toLowerCase(Locale.ROOT).startsWith("classpath:");

        try {
            List<String> lines;
            String loadedFrom;

            if (classpathConfigured) {
                String classpathLocation = normalizeClasspathLocation(configuredPath.substring("classpath:".length()));
                ClassPathResource resource = new ClassPathResource(classpathLocation);
                if (!resource.exists()) {
                    throw new IllegalStateException("Hawk monitor data file not found on classpath: classpath:" + classpathLocation);
                }
                lines = readLines(resource.getInputStream());
                loadedFrom = "classpath:" + classpathLocation;
            } else {
                Path filePath = Path.of(configuredPath);
                if (Files.exists(filePath)) {
                    lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                    loadedFrom = filePath.toString();
                } else {
                    throw new IllegalStateException("Hawk monitor data file not found on filesystem: " + configuredPath);
                }
            }

            List<HawkApplicationStatus> statuses = parseStatuses(lines);
            logger.info("Loaded {} Hawk status rows from {}", statuses.size(), loadedFrom);
            lastLoadError = null;
            return statuses;
        } catch (Exception e) {
            logger.error("Unable to load Hawk monitor file '{}': {}", configuredPath, e.getMessage());
            return loadMockStatuses(e.getMessage());
        }
    }

    private List<HawkApplicationStatus> loadMockStatuses(String reason) {
        try {
            ClassPathResource mockResource = new ClassPathResource(DEFAULT_MOCK_DATA_CLASSPATH);
            if (!mockResource.exists()) {
                throw new IllegalStateException("Mock Hawk data file not found on classpath: " + DEFAULT_MOCK_DATA_CLASSPATH);
            }

            List<String> lines = readLines(mockResource.getInputStream());
            List<HawkApplicationStatus> statuses = parseStatuses(lines);
            logger.warn("Using bundled Hawk mock data ({}). Loaded {} rows from classpath:{}",
                    reason, statuses.size(), DEFAULT_MOCK_DATA_CLASSPATH);
            lastLoadError = null;
            return statuses;
        } catch (Exception fallbackException) {
            lastLoadError = "Failed to load configured and mock Hawk data: " + fallbackException.getMessage();
            logger.error("{}", lastLoadError);
            return List.of();
        }
    }

    private List<String> readLines(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    private List<HawkApplicationStatus> parseStatuses(List<String> lines) {
        List<HawkApplicationStatus> statuses = new ArrayList<>();
        ParserState state = new ParserState();

        for (String rawLine : lines) {
            String line = safeTrim(rawLine);

            if (line.isEmpty()) {
                flushCurrentInstance(state, statuses);
                continue;
            }

            if (startsWithIgnoreCase(line, APP_PREFIX)) {
                flushCurrentInstance(state, statuses);
                state.applicationName = extractValue(line, APP_PREFIX);
                state.clearServiceContext();
                continue;
            }

            if (startsWithIgnoreCase(line, SERVICE_PREFIX)) {
                flushCurrentInstance(state, statuses);
                state.serviceName = extractValue(line, SERVICE_PREFIX);
                state.serviceDeploymentStatus = null;
                state.clearInstanceContext();
                continue;
            }

            if (startsWithIgnoreCase(line, INSTANCE_PREFIX)) {
                flushCurrentInstance(state, statuses);
                state.instanceName = extractValue(line, INSTANCE_PREFIX);
                state.instanceDeploymentStatus = null;
                state.machineName = null;
                state.instanceStatus = null;
                continue;
            }

            if (startsWithIgnoreCase(line, DEPLOYMENT_PREFIX)) {
                String deploymentStatus = extractValue(line, DEPLOYMENT_PREFIX);
                if (safeTrim(state.instanceName).isEmpty()) {
                    state.serviceDeploymentStatus = deploymentStatus;
                } else {
                    state.instanceDeploymentStatus = deploymentStatus;
                }
                continue;
            }

            if (startsWithIgnoreCase(line, MACHINE_PREFIX)) {
                state.machineName = extractValue(line, MACHINE_PREFIX);
                continue;
            }

            if (startsWithIgnoreCase(line, STATUS_PREFIX)) {
                state.instanceStatus = extractValue(line, STATUS_PREFIX);
            }
        }

        flushCurrentInstance(state, statuses);

        return statuses.stream()
                .sorted(Comparator
                        .comparing((HawkApplicationStatus item) -> safeTrim(item.getDomainName()), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(item -> safeTrim(item.getApplicationName()), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(item -> safeTrim(item.getAgentName()), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(item -> safeTrim(item.getMicroAgentName()), String.CASE_INSENSITIVE_ORDER)
                )
                .toList();
    }

    private void flushCurrentInstance(ParserState state, List<HawkApplicationStatus> statuses) {
        String applicationName = safeTrim(state.applicationName);
        String serviceName = safeTrim(state.serviceName);
        String instanceName = safeTrim(state.instanceName);

        if (applicationName.isEmpty() || serviceName.isEmpty() || instanceName.isEmpty()) {
            return;
        }

        String resolvedStatus = normalizeStatus(firstNonBlank(
                state.instanceStatus,
                state.instanceDeploymentStatus,
                state.serviceDeploymentStatus,
                "UNKNOWN"
        ));

        HawkApplicationStatus status = new HawkApplicationStatus();
        status.setDomainName(extractShip(applicationName));
        status.setApplicationName(applicationName);
        status.setAgentName(serviceName);
        status.setMicroAgentName(instanceName);
        status.setProcessId(instanceName);
        status.setHost(firstNonBlank(state.machineName, "-"));
        status.setStatus(resolvedStatus);
        status.setRunning(isRunningStatus(resolvedStatus));
        status.setVersion(firstNonBlank(state.serviceDeploymentStatus, "-"));
        status.setAdditionalInfo(buildAdditionalInfo(state));

        statuses.add(status);
        state.clearInstanceContext();
    }

    private String buildAdditionalInfo(ParserState state) {
        String serviceDeployment = firstNonBlank(state.serviceDeploymentStatus, "-");
        String instanceDeployment = firstNonBlank(state.instanceDeploymentStatus, "-");
        return "serviceDeployment=" + serviceDeployment + ", instanceDeployment=" + instanceDeployment;
    }

    private String extractShip(String applicationName) {
        String value = safeTrim(applicationName);
        if (value.isEmpty()) {
            return "UNKNOWN";
        }

        String[] parts = value.split("/");
        return parts.length > 0 ? safeTrim(parts[0]) : value;
    }

    private boolean isRunningStatus(String status) {
        String normalized = normalizeStatus(status);
        return Set.of("RUNNING", "STANDBY", "SUCCESS").contains(normalized);
    }

    private String normalizeStatus(String status) {
        return safeTrim(status).toUpperCase(Locale.ROOT);
    }

    private String normalizeClasspathLocation(String value) {
        String normalized = safeTrim(value).replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private boolean startsWithIgnoreCase(String text, String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private String extractValue(String text, String prefix) {
        if (text.length() <= prefix.length()) {
            return "";
        }
        return safeTrim(text.substring(prefix.length()));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!safeTrim(value).isEmpty()) {
                return safeTrim(value);
            }
        }
        return "";
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
