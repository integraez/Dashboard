package com.integrationhub.dashboard.service;

import com.integrationhub.dashboard.model.SystemStatusMonitor;
import com.integrationhub.dashboard.repository.SystemStatusMonitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SystemStatusMonitorService {
    
    private final SystemStatusMonitorRepository repository;
    
    public SystemStatusMonitorService(SystemStatusMonitorRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Get all system status records
     */
    public List<SystemStatusMonitor> getAllSystemStatuses() {
        return repository.findAll();
    }
    
    /**
     * Get all system status records ordered by system date time
     */
    public List<SystemStatusMonitor> getAllSystemStatusesOrderedByDateTime() {
        return repository.findAllByOrderBySsmSystemDateTimeDesc();
    }
    
    /**
     * Get system status by system name
     */
    public Optional<SystemStatusMonitor> getSystemStatusByName(String systemName) {
        return repository.findBySsmSystemName(systemName);
    }
    
    /**
     * Get system statuses by ship code
     */
    public List<SystemStatusMonitor> getSystemStatusesByShipCode(String shipCode) {
        return repository.findBySsmShipCode(shipCode);
    }
    
    /**
     * Get system status by ship code and system name
     */
    public Optional<SystemStatusMonitor> getSystemStatusByShipCodeAndName(String shipCode, String systemName) {
        return repository.findBySsmShipCodeAndSsmSystemName(shipCode, systemName);
    }
    
    /**
     * Get system status by ID
     */
    public Optional<SystemStatusMonitor> getSystemStatusById(Long id) {
        return repository.findById(id);
    }
    
    /**
     * Get all running systems
     */
    public List<SystemStatusMonitor> getRunningSystemsStatuses() {
        return repository.findBySsmSystemStatusIgnoreCase("RUNNING");
    }
    
    /**
     * Get all systems in outage
     */
    public List<SystemStatusMonitor> getOutageSystemsStatuses() {
        return repository.findBySsmSystemStatusIgnoreCase("OUTAGE");
    }
    
    /**
     * Get systems by type
     */
    public List<SystemStatusMonitor> getSystemStatusesByType(String systemType) {
        return repository.findBySsmSystemType(systemType);
    }
    
    /**
     * Check if a specific system is running
     */
    public boolean isSystemRunning(String systemName) {
        return repository.findBySsmSystemName(systemName)
                .map(SystemStatusMonitor::isRunning)
                .orElse(false);
    }
    
    /**
     * Check if a specific system is in outage
     */
    public boolean isSystemInOutage(String systemName) {
        return repository.findBySsmSystemName(systemName)
                .map(SystemStatusMonitor::isInOutage)
                .orElse(false);
    }
    
    /**
     * Get count of running systems
     */
    public long getRunningSystemsCount() {
        return repository.findBySsmSystemStatusIgnoreCase("RUNNING").size();
    }
    
    /**
     * Get count of systems in outage
     */
    public long getOutageSystemsCount() {
        return repository.findBySsmSystemStatusIgnoreCase("OUTAGE").size();
    }
}
