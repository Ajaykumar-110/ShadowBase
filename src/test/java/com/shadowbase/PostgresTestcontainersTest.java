package com.shadowbase;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresTestcontainersTest {

    @Test
    void shouldStartPostgresContainerAndConnect() throws Exception {

        try (PostgreSQLContainer<?> postgres =
                     new PostgreSQLContainer<>("postgres:16-alpine")) {
            postgres.withDatabaseName("shadowbase_test");
            postgres.withUsername("test");
            postgres.withPassword("test");

            // Automatically start PostgreSQL
            postgres.start();

            System.out.println("PostgreSQL started successfully!");
            System.out.println("JDBC URL: " + postgres.getJdbcUrl());

            // Connect Java to PostgreSQL
            try (Connection connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword())) {

                assertTrue(connection.isValid(2));

                System.out.println("Java connected to PostgreSQL!");
            }
        }
    }
}
