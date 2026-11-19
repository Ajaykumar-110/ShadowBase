
package com.shadowbase.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

class MigrationExecutorServiceTest {

    private final MigrationExecutorService executor =
            new MigrationExecutorService();

    @Test
    void shouldExecuteApprovedMigration() throws Exception {

        try (PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                var operation =
                        new MigrationPlannerService.MigrationOperation(
                                "Create employees table",
                                "CREATE TABLE employees (id INT PRIMARY KEY);",
                                MigrationPlannerService.RiskLevel.LOW,
                                false
                        );

                executor.executeApprovedMigrations(
                        connection,
                        List.of(operation),
                        true
                );

                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery(
                             "SELECT to_regclass('public.employees')")) {

                    assertTrue(result.next());
                    assertEquals("employees", result.getString(1));
                }
            }
        }
    }

    @Test
    void shouldRejectUnapprovedMigration() throws Exception {

        try (PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                var operation =
                        new MigrationPlannerService.MigrationOperation(
                                "Create employees table",
                                "CREATE TABLE employees (id INT);",
                                MigrationPlannerService.RiskLevel.LOW,
                                false
                        );

                assertThrows(
                        SecurityException.class,
                        () -> executor.executeApprovedMigrations(
                                connection,
                                List.of(operation),
                                false
                        )
                );
            }
        }
    }

    @Test
    void shouldFailForInvalidSql() throws Exception {

        try (PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                var operation =
                        new MigrationPlannerService.MigrationOperation(
                                "Invalid SQL",
                                "INVALID SQL COMMAND;",
                                MigrationPlannerService.RiskLevel.LOW,
                                false
                        );

                assertThrows(
                        SQLException.class,
                        () -> executor.executeApprovedMigrations(
                                connection,
                                List.of(operation),
                                true
                        )
                );
            }
        }
    }

    @Test
    void shouldRollbackAllChangesWhenMigrationFails()
            throws Exception {

        try (PostgreSQLContainer<?> postgres =
                new PostgreSQLContainer<>("postgres:16-alpine")) {

            postgres.start();

            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                var createTable =
                        new MigrationPlannerService.MigrationOperation(
                                "Create employees table",
                                "CREATE TABLE employees (id INT);",
                                MigrationPlannerService.RiskLevel.LOW,
                                false
                        );

                var invalidSql =
                        new MigrationPlannerService.MigrationOperation(
                                "Execute invalid SQL",
                                "INVALID SQL COMMAND;",
                                MigrationPlannerService.RiskLevel.LOW,
                                false
                        );

                assertThrows(
                        SQLException.class,
                        () -> executor.executeApprovedMigrations(
                                connection,
                                List.of(createTable, invalidSql),
                                true
                        )
                );

                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery(
                             "SELECT to_regclass('public.employees')")) {

                    assertTrue(result.next());
                    assertNull(result.getString(1));
                }
            }
        }
    }
}

