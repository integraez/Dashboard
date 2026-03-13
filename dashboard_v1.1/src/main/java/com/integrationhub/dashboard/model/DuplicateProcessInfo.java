package com.integrationhub.dashboard.model;

import java.util.List;

public class DuplicateProcessInfo {

    private final String processName;
    private final List<String> hostnames;
    private final int hostCount;

    public DuplicateProcessInfo(String processName, List<String> hostnames) {
        this.processName = processName;
        this.hostnames = hostnames;
        this.hostCount = hostnames.size();
    }

    public String getProcessName() {
        return processName;
    }

    public List<String> getHostnames() {
        return hostnames;
    }

    public int getHostCount() {
        return hostCount;
    }
}
