package com.integrationhub.dashboard.model;

import java.util.Date;

public class HawkApplicationStatus {
    
    private String domainName;
    private String agentName;
    private String microAgentName;
    private String applicationName;
    private String status;
    private String version;
    private Date timestamp;
    private String host;
    private String processId;
    private boolean running;
    private Long uptime;
    private String additionalInfo;

    public HawkApplicationStatus() {
        this.timestamp = new Date();
    }

    public HawkApplicationStatus(String domainName, String agentName, String applicationName, String status) {
        this.domainName = domainName;
        this.agentName = agentName;
        this.applicationName = applicationName;
        this.status = status;
        this.timestamp = new Date();
    }

    // Getters and Setters
    
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getMicroAgentName() {
        return microAgentName;
    }

    public void setMicroAgentName(String microAgentName) {
        this.microAgentName = microAgentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Long getUptime() {
        return uptime;
    }

    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "HawkApplicationStatus{" +
                "domainName='" + domainName + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", status='" + status + '\'' +
                ", running=" + running +
                ", timestamp=" + timestamp +
                '}';
    }
}
