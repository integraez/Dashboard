package com.integrationhub.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_status_monitoring", schema = "dbo")
public class SystemStatusMonitor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ssm_suid")
    private Long ssmSuid;
    
    @Column(name = "ssm_ship_code")
    private String ssmShipCode;
    
    @Column(name = "ssm_system_name")
    private String ssmSystemName;
    
    @Column(name = "ssm_system_type")
    private String ssmSystemType;
    
    @Column(name = "ssm_system_status")
    private String ssmSystemStatus;
    
    @Column(name = "ssm_system_date_time")
    private LocalDateTime ssmSystemDateTime;
    
    @Column(name = "ssm_created_date")
    private LocalDateTime ssmCreatedDate;
    
    @Column(name = "ssm_message")
    private String ssmMessage;
    
    // Default Constructor - Required by JPA
    public SystemStatusMonitor() {
        // Empty constructor for JPA entity instantiation
    }
    
    // Getters and Setters
    public Long getSsmSuid() {
        return ssmSuid;
    }
    
    public void setSsmSuid(Long ssmSuid) {
        this.ssmSuid = ssmSuid;
    }
    
    public String getSsmShipCode() {
        return ssmShipCode;
    }
    
    public void setSsmShipCode(String ssmShipCode) {
        this.ssmShipCode = ssmShipCode;
    }
    
    public String getSsmSystemName() {
        return ssmSystemName;
    }
    
    public void setSsmSystemName(String ssmSystemName) {
        this.ssmSystemName = ssmSystemName;
    }
    
    public String getSsmSystemType() {
        return ssmSystemType;
    }
    
    public void setSsmSystemType(String ssmSystemType) {
        this.ssmSystemType = ssmSystemType;
    }
    
    public String getSsmSystemStatus() {
        return ssmSystemStatus;
    }
    
    public void setSsmSystemStatus(String ssmSystemStatus) {
        this.ssmSystemStatus = ssmSystemStatus;
    }
    
    public LocalDateTime getSsmSystemDateTime() {
        return ssmSystemDateTime;
    }
    
    public void setSsmSystemDateTime(LocalDateTime ssmSystemDateTime) {
        this.ssmSystemDateTime = ssmSystemDateTime;
    }
    
    public LocalDateTime getSsmCreatedDate() {
        return ssmCreatedDate;
    }
    
    public void setSsmCreatedDate(LocalDateTime ssmCreatedDate) {
        this.ssmCreatedDate = ssmCreatedDate;
    }
    
    public String getSsmMessage() {
        return ssmMessage;
    }
    
    public void setSsmMessage(String ssmMessage) {
        this.ssmMessage = ssmMessage;
    }
    
    // Helper methods
    public boolean isRunning() {
        return "RUNNING".equalsIgnoreCase(ssmSystemStatus) || "UP".equalsIgnoreCase(ssmSystemStatus);
    }
    
    public boolean isInOutage() {
        return "OUTAGE".equalsIgnoreCase(ssmSystemStatus) || "DOWN".equalsIgnoreCase(ssmSystemStatus);
    }
    
    public String getStatusDisplay() {
        if (isRunning()) {
            return "Running";
        } else if (isInOutage()) {
            return "Outage";
        } else {
            return ssmSystemStatus != null ? ssmSystemStatus : "Unknown";
        }
    }
    
    public String getStatusColor() {
        if (isRunning()) {
            return "#28a745"; // Green
        } else if (isInOutage()) {
            return "#dc3545"; // Red
        } else {
            return "#6c757d"; // Gray
        }
    }
}
