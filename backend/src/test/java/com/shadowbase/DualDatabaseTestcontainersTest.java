
package com.shadowbase;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DualDatabaseTestcontainersTest {

    @Test
    void shouldConnectToSourceAndTargetDatabases() throws Exception {

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

            // Start both databases
            sourceDB.start();
            targetDB.start();

            System.out.println("Source database started!");
            System.out.println("Target database started!");

            // Connect to both databases
            try (
                Connection sourceConnection =
                    DriverManager.getConnection(
                        sourceDB.getJdbcUrl(),
                        sourceDB.getUsername(),
                        sourceDB.getPassword()
                    );

                Connection targetConnection =
                    DriverManager.getConnection(
                        targetDB.getJdbcUrl(),
                        targetDB.getUsername(),
                        targetDB.getPassword()
                    )
            ) {

                assertTrue(sourceConnection.isValid(2));
                assertTrue(targetConnection.isValid(2));

                // Create source table
                try (Statement statement = sourceConnection.createStatement()) {
                    statement.execute("""
                        CREATE TABLE employees (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100),
                            department VARCHAR(100)
                        )
                    """);

                    statement.execute("""
                        INSERT INTO employees (name, department)
                        VALUES ('Ajay', 'Engineering')
                    """);
                }

                // Verify source database
                try (Statement statement = sourceConnection.createStatement();
                     ResultSet result = statement.executeQuery(
                         "SELECT COUNT(*) FROM employees")) {

                    result.next();
                    assertEquals(1, result.getInt(1));
                }

                // Verify target database is independent
                try (Statement statement = targetConnection.createStatement();
                     ResultSet result = statement.executeQuery(
                         "SELECT COUNT(*) FROM information_schema.tables " +
                         "WHERE table_name = 'employees'")) {

                    result.next();
                    assertEquals(0, result.getInt(1));
                }

                System.out.println("Source database: Employee table created.");
                System.out.println("Target database: Independent and empty.");
                System.out.println("Both database connections successful!");
            }
        }
    }
}

