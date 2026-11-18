
package com.shadowbase.service;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

class MigrationHistoryServiceTest {

    @Test
    void shouldTrackMigrationHistory() throws Exception {

        try (PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                MigrationHistoryService service =
                        new MigrationHistoryService();

                service.initialize(connection);

                // Record successful migration
                service.recordMigration(
                        connection,
                        "Create employees table",
                        "CREATE TABLE employees (id INT);",
                        "SUCCESS",
                        null
                );

                // Record failed migration
                service.recordMigration(
                        connection,
                        "Add invalid column",
                        "INVALID SQL;",
                        "FAILED",
                        "Invalid SQL syntax"
                );

                assertTrue(service.isAlreadyExecuted(
                        connection,
                        "Create employees table"
                ));

                assertFalse(service.isAlreadyExecuted(
                        connection,
                        "Add invalid column"
                ));

                assertEquals(2, service.getTotalMigrations(connection));
                assertEquals(1, service.getSuccessfulMigrations(connection));
                assertEquals(1, service.getFailedMigrations(connection));

                System.out.println("Migration history test passed!");
            }
        }
    }
}

