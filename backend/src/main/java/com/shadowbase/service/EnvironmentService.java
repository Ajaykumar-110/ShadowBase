package com.shadowbase.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EnvironmentService {

    public Map<String, String> startEnvironment() {
        return Map.of(
            "status", "started",
            "message", "Environment started successfully"
        );
    }

    public String seedEnvironment() {
        return "Environment seed data inserted successfully";
    }

    public String stopEnvironment() {
        return "Environment stopped successfully";
    }
}
