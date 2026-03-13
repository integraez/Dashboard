package com.integrationhub.dashboard.service;

import com.integrationhub.dashboard.config.TibcoEmsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TibcoEmsService {

    private static final Logger logger = LoggerFactory.getLogger(TibcoEmsService.class);
    private final TibcoEmsProperties tibcoEmsProperties;

    public TibcoEmsService(TibcoEmsProperties tibcoEmsProperties) {
        this.tibcoEmsProperties = tibcoEmsProperties;
        logger.info("TibcoEmsService initialized with {} servers", tibcoEmsProperties.getServers().size());
        tibcoEmsProperties.getServers().forEach(s -> 
            logger.debug("  - Server: {} ({}:{})", s.getName(), s.getHost(), s.getPort())
        );
    }

    public List<TibcoEmsProperties.Server> getAllServers() {
        return tibcoEmsProperties.getServers();
    }

    public TibcoEmsProperties.Server getServerByName(String name) {
        return tibcoEmsProperties.getServers().stream()
                .filter(server -> server.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void connectToServer(TibcoEmsProperties.Server server) {
        // TODO: Implement TIBCO EMS connection logic
        System.out.println("Connecting to: " + server.getConnectionUrl());
        System.out.println("Username: " + server.getUsername());
    }
}
