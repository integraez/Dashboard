package com.integrationhub.dashboard;

import com.integrationhub.dashboard.model.DuplicateProcessInfo;
import com.integrationhub.dashboard.service.TibcoProcessMonitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tibco-process-monitor")
public class TibcoProcessMonitorController {

    private final TibcoProcessMonitorService tibcoProcessMonitorService;

    public TibcoProcessMonitorController(TibcoProcessMonitorService tibcoProcessMonitorService) {
        this.tibcoProcessMonitorService = tibcoProcessMonitorService;
    }

    @GetMapping("")
    public String processMonitorPage(Model model) {
        try {
            List<DuplicateProcessInfo> duplicates = tibcoProcessMonitorService.getDuplicateProcesses();
            model.addAttribute("duplicates", duplicates);
            model.addAttribute("dataError", null);
        } catch (Exception e) {
            model.addAttribute("duplicates", List.of());
            model.addAttribute("dataError", "Unable to load duplicate process data: " + e.getMessage());
        }
        return "tibco-process-monitor";
    }

    @GetMapping("/api/duplicates")
    @ResponseBody
    public ResponseEntity<?> getDuplicateProcesses() {
        try {
            List<DuplicateProcessInfo> duplicates = tibcoProcessMonitorService.getDuplicateProcesses();
            return ResponseEntity.ok(duplicates);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Configuration/Data Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal Error");
            error.put("message", "Unable to load duplicate process data");
            error.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
