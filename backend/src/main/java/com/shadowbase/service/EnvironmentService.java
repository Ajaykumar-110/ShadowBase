
package com.shadowbase.service;

import org.springframework.stereotype.Service;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@Service
public class EnvironmentService {

    private PostgreSQLContainer<?> postgres;

    public Map<String, String> startEnvironment() {

        if (postgres != null && postgres.isRunning()) {
            return Map.of(
                "status", "already_running",
                "message", "PostgreSQL environment is already running",
                "containerId", postgres.getContainerId()
            );
        }

        postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("shadowbase")
                .withUsername("shadowbase")
                .withPassword("shadowbase");

        postgres.start();

        return Map.of(
            "status", "started",
            "message", "PostgreSQL container started successfully",
            "containerId", postgres.getContainerId(),
            "jdbcUrl", postgres.getJdbcUrl()
        );
    }

    public String seedEnvironment() {
        return "Database seeding is not implemented yet";
    }

    public String stopEnvironment() {

        if (postgres == null || !postgres.isRunning()) {
            return "No running PostgreSQL environment found";
        }

        postgres.stop();
        postgres = null;

        return "PostgreSQL container stopped successfully";
    }
}
