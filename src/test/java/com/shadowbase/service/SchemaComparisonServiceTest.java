
package com.shadowbase.service;

import com.shadowbase.service.SchemaComparisonService.SchemaDifference;
import com.shadowbase.service.SchemaComparisonService.DifferenceType;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaComparisonServiceTest {

    @Test
    void shouldDetectSchemaDifferences() throws Exception {

        try (
            PostgreSQLContainer<?> sourceDB =
                new PostgreSQLContainer<>("postgres:16-alpine");

            PostgreSQLContainer<?> targetDB =
                new PostgreSQLContainer<>("postgres:16-alpine")
        ) {
            sourceDB.withDatabaseName("source_db");
            sourceDB.withUsername("test");
            sourceDB.withPassword("test");

            targetDB.withDatabaseName("target_db");
            targetDB.withUsername("test");
            targetDB.withPassword("test");

            sourceDB.start();
            targetDB.start();

            try (
                Connection source = DriverManager.getConnection(
                    sourceDB.getJdbcUrl(),
                    sourceDB.getUsername(),
                    sourceDB.getPassword()
                );

                Connection target = DriverManager.getConnection(
                    targetDB.getJdbcUrl(),
                    targetDB.getUsername(),
                    targetDB.getPassword()
                )
            ) {

                try (Statement statement = source.createStatement()) {
                    statement.execute("""
                        CREATE TABLE employees (
                            id INTEGER PRIMARY KEY,
                            name VARCHAR(100),
                            department VARCHAR(100)
                        )
                    """);
                }

                try (Statement statement = target.createStatement()) {
                    statement.execute("""
                        CREATE TABLE employees (
                            id INTEGER PRIMARY KEY,
                            name VARCHAR(100),
                            email VARCHAR(150)
                        )
                    """);
                }

                SchemaComparisonService service =
                        new SchemaComparisonService();

                List<SchemaDifference> differences =
                        service.compare(source, target);

                differences.forEach(System.out::println);

                assertTrue(differences.stream().anyMatch(
                    d -> d.type() ==
                        DifferenceType.MISSING_COLUMN_IN_TARGET
                        && "department".equals(d.columnName())
                ));

                assertTrue(differences.stream().anyMatch(
                    d -> d.type() ==
                        DifferenceType.EXTRA_COLUMN_IN_TARGET
                        && "email".equals(d.columnName())
                ));

                System.out.println(
                    "Schema comparison completed successfully!"
                );
            }
        }
    }
}

