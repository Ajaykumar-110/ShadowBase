package com.shadowbase.controller;

import com.shadowbase.service.EnvironmentService;


import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/environments")
public class EnvironmentController {

    private final EnvironmentService environmentService;

    public EnvironmentController(
            EnvironmentService environmentService) {

        this.environmentService = environmentService;
    }

    // Create environment
    @PostMapping("/start")
    public Map<String, String> startEnvironment() {

        return environmentService.startEnvironment();
    }

    // Insert sample data
    @PostMapping("/seed")
    public Map<String, String> seedEnvironment()
            throws Exception {

        String message =
                environmentService.seedEnvironment();

        return Map.of("message", message);
    }

    // Destroy environment
    @DeleteMapping("/stop")
    public Map<String, String> stopEnvironment() {

        String message =
                environmentService.stopEnvironment();

        return Map.of("message", message);
    }
}
