package com.integrationhub.dashboard.model;

public class QueueInfo {
    private String serverName;
    private String queueName;
    private long messageCount;
    private String status;

    public QueueInfo(String serverName, String queueName, long messageCount) {
        this.serverName = serverName;
        this.queueName = queueName;
        this.messageCount = messageCount;
        this.status = determineStatus(messageCount);
    }

    private String determineStatus(long count) {
        if (count > 10000) {
            return "critical";
        } else if (count > 5000) {
            return "warning";
        } else {
            return "ok";
        }
    }

    public String getServerName() {
        return serverName;
    }

    public String getQueueName() {
        return queueName;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusIcon() {
        return switch (status) {
            case "critical" -> "🔴";
            case "warning" -> "⚠️";
            default -> "✅";
        };
    }

    public String getStatusColor() {
        return switch (status) {
            case "critical" -> "rgba(255,99,99,0.95)";
            case "warning" -> "rgba(255,193,7,0.95)";
            default -> "rgba(35,196,131,0.95)";
        };
    }
}
