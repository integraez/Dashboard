package com.integrationhub.dashboard;

import com.integrationhub.dashboard.config.TibcoEmsProperties;
import com.integrationhub.dashboard.model.QueueInfo;
import com.integrationhub.dashboard.service.TibcoEmsQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final TibcoEmsQueueService tibcoEmsQueueService;

    public DashboardController(TibcoEmsQueueService tibcoEmsQueueService) {
        this.tibcoEmsQueueService = tibcoEmsQueueService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/api/queues")
    @ResponseBody
    public List<QueueInfo> getQueuesApi() {
        return tibcoEmsQueueService.getHighVolumeQueues();
    }

    @GetMapping("/api/status")
    @ResponseBody
    public String getStatus() {
        return """
                {
                  "tibcoLibAvailable": %s,
                  "classPath": "%s",
                  "serverCount": %d
                }
                """.formatted(
                checkTibcoLib(),
                System.getProperty("java.class.path").replace("\\", "\\\\"),
                tibcoEmsQueueService.getAllServers().size()
        );
    }

    @GetMapping("/api/test-connection")
    @ResponseBody
    public String testConnection() {
        StringBuilder result = new StringBuilder();
        var servers = tibcoEmsQueueService.getAllServers();
        result.append("Testing ").append(servers.size()).append(" servers:\n\n");
        
        for (var server : servers) {
            result.append(server.getName()).append(" (").append(server.getHost())
                  .append(":").append(server.getPort()).append(")\n");
            var queues = tibcoEmsQueueService.getAllQueuesForServer(server.getName());
            result.append("  Queues found: ").append(queues.size()).append("\n");
            if (!queues.isEmpty()) {
                result.append("  Top 3:\n");
                queues.stream().limit(3).forEach(q -> 
                    result.append("    - ").append(q.getQueueName())
                          .append(": ").append(q.getMessageCount()).append(" msgs\n")
                );
            }
            result.append("\n");
        }
        
        return result.toString();
    }

    private boolean checkTibcoLib() {
        try {
            Class.forName("com.tibco.tibjms.admin.TibjmsAdmin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @GetMapping("/api/queues/{serverName}")
    @ResponseBody
    public List<QueueInfo> getQueuesForServer(@PathVariable String serverName) {
        return tibcoEmsQueueService.getAllQueuesForServer(serverName);
    }

    @GetMapping("/api/configured-servers")
    @ResponseBody
    public List<Map<String, String>> getConfiguredServers() {
        Map<String, String> serverStatusMap = tibcoEmsQueueService.getServerStatus();
        return tibcoEmsQueueService.getAllServers()
                .stream()
                .map(server -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", server.getName());
                    map.put("status", serverStatusMap.getOrDefault(server.getName(), "UNKNOWN"));
                    return map;
                })
                .toList();
    }
}
