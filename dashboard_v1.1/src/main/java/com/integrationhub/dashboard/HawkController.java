package com.integrationhub.dashboard;

import com.integrationhub.dashboard.model.HawkApplicationStatus;
import com.integrationhub.dashboard.service.TibcoHawkService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/hawk")
public class HawkController {

    private final TibcoHawkService tibcoHawkService;

    public HawkController(TibcoHawkService tibcoHawkService) {
        this.tibcoHawkService = tibcoHawkService;
    }

    /**
     * View page for Hawk application monitor
     */
    @GetMapping("/monitor")
    public String monitorPage(
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        List<HawkApplicationStatus> allApplications = tibcoHawkService.getAllApplicationStatuses();
        String selectedStatus = normalizeStatusFilter(status);

        List<HawkApplicationStatus> filteredApplications = allApplications.stream()
                .filter(app -> {
                    if ("running".equals(selectedStatus)) {
                        return app.isRunning();
                    }
                    if ("stopped".equals(selectedStatus)) {
                        return isStoppedStatus(app);
                    }
                    if ("other".equals(selectedStatus)) {
                        return isOtherStatus(app);
                    }
                    return true;
                })
                .toList();

        long stoppedCount = allApplications.stream().filter(this::isStoppedStatus).count();
        long otherCount = allApplications.stream().filter(this::isOtherStatus).count();
        long runningCount = allApplications.size() - stoppedCount - otherCount;
        long shipCount = allApplications.stream()
                .map(HawkApplicationStatus::getHost)
                .filter(host -> host != null && host.trim().length() >= 2)
                .map(host -> host.trim().substring(0, 2).toUpperCase())
                .collect(Collectors.toSet())
                .size();

        model.addAttribute("allApplicationsData", allApplications);
        model.addAttribute("initialApplications", filteredApplications);
        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("totalApplications", allApplications.size());
        model.addAttribute("runningApplications", runningCount);
        model.addAttribute("stoppedApplications", stoppedCount);
        model.addAttribute("otherApplications", otherCount);
        model.addAttribute("totalShips", shipCount);
        model.addAttribute("filteredCount", filteredApplications.size());
        model.addAttribute("statusDescription", buildStatusDescription(selectedStatus, filteredApplications.size()));
        return "hawk-monitor";
    }

    private String buildStatusDescription(String selectedStatus, int filteredCount) {
        if ("running".equals(selectedStatus)) {
            return "Showing " + filteredCount + " running/standby services.";
        }
        if ("stopped".equals(selectedStatus)) {
            return "Showing " + filteredCount + " stopped/unknown services.";
        }
        if ("other".equals(selectedStatus)) {
            return "Showing " + filteredCount + " services in other states.";
        }
        return "Showing " + filteredCount + " services across all statuses.";
    }

    private boolean isStoppedStatus(HawkApplicationStatus app) {
        String normalized = normalizeStatus(app.getStatus());
        return "STOPPED".equals(normalized) || "UNKNOWN".equals(normalized);
    }

    private boolean isOtherStatus(HawkApplicationStatus app) {
        String normalized = normalizeStatus(app.getStatus());
        boolean running = app.isRunning() || "RUNNING".equals(normalized) || "STANDBY".equals(normalized) || "SUCCESS".equals(normalized);
        boolean stopped = "STOPPED".equals(normalized) || "UNKNOWN".equals(normalized);
        return !running && !stopped;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private String normalizeStatusFilter(String status) {
        if (status == null) {
            return "";
        }

        String normalized = status.trim().toLowerCase();
        return Set.of("running", "stopped", "other").contains(normalized) ? normalized : "";
    }

    /**
     * Get all application statuses from all configured TIBCO Hawk domains
     */
    @GetMapping("/api/applications")
    @ResponseBody
    public List<HawkApplicationStatus> getAllApplications() {
        return tibcoHawkService.getAllApplicationStatuses();
    }

    /**
     * Get application statuses for a specific domain
     */
    @GetMapping("/api/applications/domain/{domainName}")
    @ResponseBody
    public List<HawkApplicationStatus> getApplicationsByDomain(@PathVariable String domainName) {
        return tibcoHawkService.getApplicationStatusesForDomain(domainName);
    }

    /**
     * Get all configured domains
     */
    @GetMapping("/api/domains")
    @ResponseBody
    public List<Map<String, String>> getConfiguredDomains() {
        Map<String, String> domainStatusMap = tibcoHawkService.getDomainStatus();
        return tibcoHawkService.getAllDomains()
                .stream()
                .map(domain -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("name", domain.getName());
                    map.put("rvService", domain.getRvService());
                    map.put("rvNetwork", domain.getRvNetwork());
                    map.put("rvDaemon", domain.getRvDaemon());
                    map.put("status", domainStatusMap.getOrDefault(domain.getName(), "UNKNOWN"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get summary statistics across all domains
     */
    @GetMapping("/api/summary")
    @ResponseBody
    public Map<String, Object> getSummary() {
        List<HawkApplicationStatus> allApps = tibcoHawkService.getAllApplicationStatuses();
        
        long totalApps = allApps.size();
        long stoppedApps = allApps.stream().filter(this::isStoppedStatus).count();
        long otherApps = allApps.stream().filter(this::isOtherStatus).count();
        long runningApps = totalApps - stoppedApps - otherApps;
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalApplications", totalApps);
        summary.put("runningApplications", runningApps);
        summary.put("stoppedApplications", stoppedApps);
        summary.put("otherApplications", otherApps);
        summary.put("totalDomains", tibcoHawkService.getAllDomains().size());
        summary.put("timestamp", new java.util.Date());
        
        // Group by domain
        Map<String, Long> appsByDomain = allApps.stream()
                .collect(Collectors.groupingBy(
                        HawkApplicationStatus::getDomainName,
                        Collectors.counting()
                ));
        summary.put("applicationsByDomain", appsByDomain);
        
        return summary;
    }

    /**
     * Get only running applications
     */
    @GetMapping("/api/applications/running")
    @ResponseBody
    public List<HawkApplicationStatus> getRunningApplications() {
        return tibcoHawkService.getAllApplicationStatuses()
                .stream()
                .filter(HawkApplicationStatus::isRunning)
                .collect(Collectors.toList());
    }

    /**
     * Get only stopped applications
     */
    @GetMapping("/api/applications/stopped")
    @ResponseBody
    public List<HawkApplicationStatus> getStoppedApplications() {
        return tibcoHawkService.getAllApplicationStatuses()
                .stream()
                .filter(app -> !app.isRunning())
                .collect(Collectors.toList());
    }

    /**
     * Get applications by host
     */
    @GetMapping("/api/applications/host/{hostname}")
    @ResponseBody
    public List<HawkApplicationStatus> getApplicationsByHost(@PathVariable String hostname) {
        return tibcoHawkService.getAllApplicationStatuses()
                .stream()
                .filter(app -> app.getHost() != null && app.getHost().contains(hostname))
                .collect(Collectors.toList());
    }

    /**
     * Test connection to all Hawk domains
     */
    @GetMapping("/api/test-connection")
    @ResponseBody
    public Map<String, String> testConnection() {
        return tibcoHawkService.getDomainStatus();
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "TibcoHawkService");
        health.put("timestamp", new java.util.Date());
        
        Map<String, String> domainStatus = tibcoHawkService.getDomainStatus();
        long healthyDomains = domainStatus.values().stream()
                .filter(status -> status.equals("CONNECTED") || status.equals("MOCK_MODE"))
                .count();
        
        health.put("configuredDomains", domainStatus.size());
        health.put("healthyDomains", healthyDomains);
        health.put("domainStatus", domainStatus);
        
        return health;
    }
}
