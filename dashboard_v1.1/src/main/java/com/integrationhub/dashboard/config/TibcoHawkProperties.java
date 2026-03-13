package com.integrationhub.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "tibco.hawk")
public class TibcoHawkProperties {

    private String dataFile;
    private List<Domain> domains = new ArrayList<>();

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }

    public static class Domain {
        private String name;
        private String rvService;
        private String rvNetwork;
        private String rvDaemon;
        private int timeout = 5000; // Default 5 seconds

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRvService() {
            return rvService;
        }

        public void setRvService(String rvService) {
            this.rvService = rvService;
        }

        public String getRvNetwork() {
            return rvNetwork;
        }

        public void setRvNetwork(String rvNetwork) {
            this.rvNetwork = rvNetwork;
        }

        public String getRvDaemon() {
            return rvDaemon;
        }

        public void setRvDaemon(String rvDaemon) {
            this.rvDaemon = rvDaemon;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
