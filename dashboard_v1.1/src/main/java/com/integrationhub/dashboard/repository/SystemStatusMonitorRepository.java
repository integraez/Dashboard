package com.integrationhub.dashboard.repository;

import com.integrationhub.dashboard.model.SystemStatusMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemStatusMonitorRepository extends JpaRepository<SystemStatusMonitor, Long> {
    
    // Find by system name
    Optional<SystemStatusMonitor> findBySsmSystemName(String ssmSystemName);
    
    // Find by ship code
    List<SystemStatusMonitor> findBySsmShipCode(String ssmShipCode);
    
    // Find all by system status
    List<SystemStatusMonitor> findBySsmSystemStatusIgnoreCase(String status);
    
    // Find by ship code and system name
    Optional<SystemStatusMonitor> findBySsmShipCodeAndSsmSystemName(String ssmShipCode, String ssmSystemName);
    
    // Find all systems ordered by system date time
    List<SystemStatusMonitor> findAllByOrderBySsmSystemDateTimeDesc();
    
    // Find by system type
    List<SystemStatusMonitor> findBySsmSystemType(String ssmSystemType);
}
