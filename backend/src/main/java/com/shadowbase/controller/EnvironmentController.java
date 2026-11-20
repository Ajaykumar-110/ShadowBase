package com.shadowbase.controller;

import com.shadowbase.service.EnvironmentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @PostMapping("/start")
    public Map<String, String> startEnvironment() {
        return environmentService.startEnvironment();
    }

    @PostMapping("/seed")
    public Map<String, String> seedEnvironment() {
        String message = environmentService.seedEnvironment();
        return Map.of("message", message);
    }

    @DeleteMapping("/stop")
    public Map<String, String> stopEnvironment() {
        String message = environmentService.stopEnvironment();
        return Map.of("message", message);
    }
}
