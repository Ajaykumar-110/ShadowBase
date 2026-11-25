
package com.shadowbase.service;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MigrationExecutorServiceTest {

    private final MigrationHistoryService historyService =
            new MigrationHistoryService();

    private final MigrationExecutorService executor =
            new MigrationExecutorService(historyService);

    @Test
    void shouldExecuteApprovedMigration() throws Exception {

        try (PostgreSQLContainer<?> postgres =
                     new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                MigrationPlannerService.MigrationOperation operation =
                        new MigrationPlannerService.MigrationOperation(
                                "Create employees table",
                                "CREATE TABLE employees (id INT);",
                                MigrationPlannerService.RiskLevel.LOW,
                                false
                        );

                executor.executeApprovedMigrations(
                        connection,
                        List.of(operation),
                        true
                );

                assertTrue(historyService.isAlreadyExecuted(
                        connection,
                        "Create employees table"
                ));

                assertEquals(
                        1,
                        historyService.getSuccessfulMigrations(connection)
                );

                System.out.println("Migration execution test passed!");
            }
        }
    }

    @Test
    void shouldRejectUnapprovedMigration() {

        assertThrows(
                SecurityException.class,
                () -> executor.executeApprovedMigrations(
                        null,
                        null,
                        false
                )
        );
    }
}
