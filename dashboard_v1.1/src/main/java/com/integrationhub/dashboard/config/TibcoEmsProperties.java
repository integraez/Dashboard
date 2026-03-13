package com.integrationhub.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tibco.ems", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class TibcoEmsProperties {

    private static final Logger logger = LoggerFactory.getLogger(TibcoEmsProperties.class);
    private List<Server> servers = new ArrayList<>();

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
        logger.info("setServers() called with {} servers", servers.size());
        servers.forEach(s -> logger.debug("  Loaded server: {} at {}:{}", s.getName(), s.getHost(), s.getPort()));
    }

    public static class Server {
        private String name;
        private String host;
        private int port;
        private String username;
        private String password;
        private boolean sslEnabled;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public void setSslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
        }

        public String getConnectionUrl() {
            String protocol = sslEnabled ? "ssl" : "tcp";
            return protocol + "://" + host + ":" + port;
        }
    }
}
