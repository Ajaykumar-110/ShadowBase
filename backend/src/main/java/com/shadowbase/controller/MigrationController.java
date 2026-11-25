
package com.shadowbase.controller;

import com.shadowbase.service.SchemaComparisonService;
import com.shadowbase.service.MigrationPlannerService;
import com.shadowbase.service.MigrationExecutorService;
import com.shadowbase.service.EnvironmentService;

import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final SchemaComparisonService comparisonService;
    private final MigrationPlannerService plannerService;
    private final MigrationExecutorService executorService;
    private final EnvironmentService environmentService;

    public MigrationController(
            SchemaComparisonService comparisonService,
            MigrationPlannerService plannerService,
            MigrationExecutorService executorService,
            EnvironmentService environmentService) {

        this.comparisonService = comparisonService;
        this.plannerService = plannerService;
        this.executorService = executorService;
        this.environmentService = environmentService;
    }

    // Check application status
    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
                "project", "ShadowBase",
                "module", "Migration Engine",
                "status", "Available"
        );
    }

    // Compare source and target database schemas
    @GetMapping("/compare")
    public List<SchemaComparisonService.SchemaDifference> compareSchemas()
            throws SQLException {

        try (
                Connection source =
                        environmentService.getSourceConnection();
                Connection target =
                        environmentService.getTargetConnection()
        ) {
            return comparisonService.compare(source, target);
        }
    }

    // Generate migration plan without executing SQL
    @GetMapping("/plan")
    public List<MigrationPlannerService.MigrationOperation> generatePlan()
            throws SQLException {

        try (
                Connection source =
                        environmentService.getSourceConnection();
                Connection target =
                        environmentService.getTargetConnection()
        ) {
            var differences =
                    comparisonService.compare(source, target);

            return plannerService.generatePlan(differences);
        }
    }
}
