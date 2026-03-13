package com.integrationhub.dashboard;

import com.integrationhub.dashboard.model.SystemStatusMonitor;
import com.integrationhub.dashboard.service.SystemStatusMonitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/endpoints-ship")
public class EndpointsShipController {
    
    private final SystemStatusMonitorService systemStatusMonitorService;
    
    public EndpointsShipController(SystemStatusMonitorService systemStatusMonitorService) {
        this.systemStatusMonitorService = systemStatusMonitorService;
    }
    
    /**
     * Serve the endpoints-ship HTML page
     * GET /endpoints-ship
     */
    @GetMapping("")
    public String endpointsShipPage() {
        return "endpoints-ship";
    }
    
    /**
     * Get all system statuses
     * GET /endpoints-ship/api/status
     */
    @GetMapping("/api/status")
    @ResponseBody
    public ResponseEntity<?> getAllSystemStatuses() {
        try {
            List<SystemStatusMonitor> statuses = systemStatusMonitorService.getAllSystemStatusesOrderedByDateTime();
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to retrieve system status data. Please check database connectivity.");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Get system statuses by ship code
     * GET /endpoints-ship/api/ship/{shipCode}
     */
    @GetMapping("/api/ship/{shipCode}")
    @ResponseBody
    public ResponseEntity<?> getSystemStatusesByShipCode(@PathVariable String shipCode) {
        try {
            List<SystemStatusMonitor> statuses = systemStatusMonitorService.getSystemStatusesByShipCode(shipCode);
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to retrieve system status data for ship: " + shipCode);
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Get system status by system name
     * GET /endpoints-ship/api/status/{systemName}
     */
    @GetMapping("/api/status/{systemName}")
    @ResponseBody
    public ResponseEntity<?> getSystemStatusByName(@PathVariable String systemName) {
        try {
            return systemStatusMonitorService.getSystemStatusByName(systemName)
                    .map(status -> ResponseEntity.ok((Object) status))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to retrieve system status for: " + systemName);
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Get all running systems
     * GET /endpoints-ship/api/running
     */
    @GetMapping("/api/running")
    @ResponseBody
    public ResponseEntity<?> getRunningSystemStatuses() {
        try {
            List<SystemStatusMonitor> runningStatuses = systemStatusMonitorService.getRunningSystemsStatuses();
            return ResponseEntity.ok(runningStatuses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to retrieve running systems data.");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Get all systems in outage
     * GET /endpoints-ship/api/outage
     */
    @GetMapping("/api/outage")
    @ResponseBody
    public ResponseEntity<?> getOutageSystemStatuses() {
        try {
            List<SystemStatusMonitor> outageStatuses = systemStatusMonitorService.getOutageSystemsStatuses();
            return ResponseEntity.ok(outageStatuses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to retrieve outage systems data.");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Get systems by type
     * GET /endpoints-ship/api/type/{systemType}
     */
    @GetMapping("/api/type/{systemType}")
    @ResponseBody
    public ResponseEntity<?> getSystemStatusesByType(@PathVariable String systemType) {
        try {
            List<SystemStatusMonitor> statuses = systemStatusMonitorService.getSystemStatusesByType(systemType);
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to retrieve system status data for type: " + systemType);
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Check if a specific system is running or in outage
     * GET /endpoints-ship/api/check/{systemName}
     */
    @GetMapping("/api/check/{systemName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSystemStatus(@PathVariable String systemName) {
        try {
            return systemStatusMonitorService.getSystemStatusByName(systemName)
                    .map(status -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("ssmSuid", status.getSsmSuid());
                        response.put("ssmShipCode", status.getSsmShipCode());
                        response.put("ssmSystemName", status.getSsmSystemName());
                        response.put("ssmSystemType", status.getSsmSystemType());
                        response.put("ssmSystemStatus", status.getSsmSystemStatus());
                        response.put("ssmSystemDateTime", status.getSsmSystemDateTime());
                        response.put("ssmCreatedDate", status.getSsmCreatedDate());
                        response.put("ssmMessage", status.getSsmMessage());
                        response.put("isRunning", status.isRunning());
                        response.put("isInOutage", status.isInOutage());
                        response.put("statusDisplay", status.getStatusDisplay());
                        response.put("statusColor", status.getStatusColor());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "System not found: " + systemName)));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to check system status for: " + systemName);
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Check system status by ship code and system name
     * GET /endpoints-ship/api/check/{shipCode}/{systemName}
     */
    @GetMapping("/api/check/{shipCode}/{systemName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkSystemStatusByShipCodeAndName(
            @PathVariable String shipCode, 
            @PathVariable String systemName) {
        try {
            return systemStatusMonitorService.getSystemStatusByShipCodeAndName(shipCode, systemName)
                    .map(status -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("ssmSuid", status.getSsmSuid());
                        response.put("ssmShipCode", status.getSsmShipCode());
                        response.put("ssmSystemName", status.getSsmSystemName());
                        response.put("ssmSystemType", status.getSsmSystemType());
                        response.put("ssmSystemStatus", status.getSsmSystemStatus());
                        response.put("ssmSystemDateTime", status.getSsmSystemDateTime());
                        response.put("ssmCreatedDate", status.getSsmCreatedDate());
                        response.put("ssmMessage", status.getSsmMessage());
                        response.put("isRunning", status.isRunning());
                        response.put("isInOutage", status.isInOutage());
                        response.put("statusDisplay", status.getStatusDisplay());
                        response.put("statusColor", status.getStatusColor());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "System not found for ship: " + shipCode + ", system: " + systemName)));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("message", "Unable to check system status for ship: " + shipCode + ", system: " + systemName);
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Get summary statistics
     * GET /endpoints-ship/api/summary
     */
    @GetMapping("/api/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalSystems", systemStatusMonitorService.getAllSystemStatuses().size());
            summary.put("runningCount", systemStatusMonitorService.getRunningSystemsCount());
            summary.put("outageCount", systemStatusMonitorService.getOutageSystemsCount());
            summary.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Database connection failed");
            error.put("totalSystems", 0);
            error.put("runningCount", 0);
            error.put("outageCount", 0);
            error.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
    
    /**
     * Health check endpoint
     * GET /endpoints-ship/api/health
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "EndpointsShip");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
