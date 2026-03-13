package com.integrationhub.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class ProcessMonitorEntry {

    @JsonProperty("duplicate_processes")
    @JsonDeserialize(using = DuplicateProcessesDeserializer.class)
    private List<String> duplicateProcesses;

    private String server;

    public List<String> getDuplicateProcesses() {
        return duplicateProcesses;
    }

    public void setDuplicateProcesses(List<String> duplicateProcesses) {
        this.duplicateProcesses = duplicateProcesses;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
