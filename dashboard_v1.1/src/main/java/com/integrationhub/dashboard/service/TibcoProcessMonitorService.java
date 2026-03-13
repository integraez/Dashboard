package com.integrationhub.dashboard.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationhub.dashboard.model.DuplicateProcessInfo;
import com.integrationhub.dashboard.model.ProcessMonitorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class TibcoProcessMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(TibcoProcessMonitorService.class);
    private static final Set<String> NO_DUPLICATE_MARKERS = Set.of(
            "no duplicates",
            "no duplicate",
            "none",
            "n/a"
    );

    private final ObjectMapper objectMapper;
    private final String processMonitorFilePath;

    public TibcoProcessMonitorService(
            @Value("${tibco.process-monitor.data-file:${TIBCO_PROCESS_MONITOR_FILE:}}") String processMonitorFilePath) {
        this.objectMapper = new ObjectMapper();
        this.processMonitorFilePath = processMonitorFilePath;
    }

    public List<DuplicateProcessInfo> getDuplicateProcesses() {
        List<ProcessMonitorEntry> entries = loadEntries();

        Map<String, Set<String>> processToHosts = new LinkedHashMap<>();
        for (ProcessMonitorEntry entry : entries) {
            if (entry == null) {
                continue;
            }

            String hostname = safeTrim(entry.getServer());
            List<String> duplicateProcesses = entry.getDuplicateProcesses();
            if (hostname.isEmpty() || duplicateProcesses == null || duplicateProcesses.isEmpty()) {
                continue;
            }

            for (String processNameToken : duplicateProcesses) {
                String processName = safeTrim(processNameToken);
                if (processName.isEmpty() || isNoDuplicateValue(processName)) {
                    continue;
                }

                processToHosts
                        .computeIfAbsent(processName, ignored -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))
                        .add(hostname);
            }
        }

        return processToHosts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> new DuplicateProcessInfo(entry.getKey(), new ArrayList<>(entry.getValue())))
                .toList();
    }

    private List<ProcessMonitorEntry> loadEntries() {
        if (safeTrim(processMonitorFilePath).isEmpty()) {
            throw new IllegalStateException("Process monitor data file path is not configured. Set tibco.process-monitor.data-file or TIBCO_PROCESS_MONITOR_FILE.");
        }

        String configuredPath = safeTrim(processMonitorFilePath);
        boolean classpathConfigured = configuredPath.toLowerCase(Locale.ROOT).startsWith("classpath:");
        String classpathLocation = classpathConfigured
                ? normalizeClasspathLocation(configuredPath.substring("classpath:".length()))
                : normalizeClasspathLocation(configuredPath);

        try {
            List<ProcessMonitorEntry> entries;
            String loadedFrom;

            if (classpathConfigured) {
                ClassPathResource resource = new ClassPathResource(classpathLocation);
                if (!resource.exists()) {
                    throw new IllegalStateException("Process monitor data file not found on server check ansible playbook in JB:");
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    entries = objectMapper.readValue(inputStream, new TypeReference<List<ProcessMonitorEntry>>() {
                    });
                }
                loadedFrom = "classpath:" + classpathLocation;
            } else {
                Path filePath = Path.of(configuredPath);
                if (Files.exists(filePath)) {
                    entries = objectMapper.readValue(
                            filePath.toFile(),
                            new TypeReference<List<ProcessMonitorEntry>>() {
                            }
                    );
                    loadedFrom = filePath.toString();
                } else {
                    ClassPathResource fallbackResource = new ClassPathResource(classpathLocation);
                    if (!fallbackResource.exists()) {
                        throw new IllegalStateException("Process monitor data file not found: " + configuredPath + " (filesystem or classpath)");
                    }
                    try (InputStream inputStream = fallbackResource.getInputStream()) {
                        entries = objectMapper.readValue(inputStream, new TypeReference<List<ProcessMonitorEntry>>() {
                        });
                    }
                    loadedFrom = "classpath:" + classpathLocation;
                    logger.info("Process monitor file not found on filesystem at '{}'; loaded from classpath instead.", configuredPath);
                }
            }

            logger.info("Loaded {} process-monitor rows from {}", entries == null ? 0 : entries.size(), loadedFrom);
            if (entries == null) {
                return List.of();
            }

            return entries.stream()
                    .sorted(Comparator.comparing(entry -> safeTrim(entry.getServer()), String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse process monitor JSON file: " + configuredPath, e);
        }
    }

    private String normalizeClasspathLocation(String value) {
        String normalized = safeTrim(value).replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private boolean isNoDuplicateValue(String value) {
        return NO_DUPLICATE_MARKERS.contains(value.toLowerCase(Locale.ROOT));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
