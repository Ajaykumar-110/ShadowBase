
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

                // SOURCE DATABASE
                try (Statement statement = source.createStatement()) {

                    statement.execute("""
                        CREATE TABLE employees (
                            id INTEGER PRIMARY KEY,
                            name VARCHAR(100),
                            department VARCHAR(100),
                            salary INTEGER
                        )
                    """);

                    statement.execute("""
                        CREATE TABLE source_only (
                            id INTEGER PRIMARY KEY
                        )
                    """);
                }

                // TARGET DATABASE
                try (Statement statement = target.createStatement()) {

                    statement.execute("""
                        CREATE TABLE employees (
                            id INTEGER PRIMARY KEY,
                            name VARCHAR(100),
                            email VARCHAR(150),
                            salary VARCHAR(50)
                        )
                    """);

                    statement.execute("""
                        CREATE TABLE target_only (
                            id INTEGER PRIMARY KEY
                        )
                    """);
                }

                // COMPARE SCHEMAS
                SchemaComparisonService service =
                        new SchemaComparisonService();

                List<SchemaDifference> differences =
                        service.compare(source, target);

                differences.forEach(System.out::println);

                // 1. Missing column
                assertTrue(differences.stream().anyMatch(
                    d -> d.type() == DifferenceType.MISSING_COLUMN_IN_TARGET
                        && "department".equals(d.columnName())
                ));

                // 2. Extra column
                assertTrue(differences.stream().anyMatch(
                    d -> d.type() == DifferenceType.EXTRA_COLUMN_IN_TARGET
                        && "email".equals(d.columnName())
                ));

                // 3. Missing table
                assertTrue(differences.stream().anyMatch(
                    d -> d.type() == DifferenceType.MISSING_TABLE_IN_TARGET
                        && "source_only".equals(d.tableName())
                ));

                // 4. Extra table
                assertTrue(differences.stream().anyMatch(
                    d -> d.type() == DifferenceType.EXTRA_TABLE_IN_TARGET
                        && "target_only".equals(d.tableName())
                ));

                // 5. Column type mismatch
                assertTrue(differences.stream().anyMatch(
                    d -> d.type() == DifferenceType.COLUMN_TYPE_MISMATCH
                        && "salary".equals(d.columnName())
                ));

                System.out.println(
                    "All five schema comparison scenarios verified!"
                );
            }
        }
    }
}
