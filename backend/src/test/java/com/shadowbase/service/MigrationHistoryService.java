
package com.shadowbase.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class MigrationHistoryService {

    private static final String CREATE_HISTORY_TABLE = """
            CREATE TABLE IF NOT EXISTS migration_history (
                id BIGSERIAL PRIMARY KEY,
                migration_description TEXT NOT NULL,
                migration_sql TEXT NOT NULL,
                status VARCHAR(20) NOT NULL,
                executed_at TIMESTAMP,
                error_message TEXT,
                CONSTRAINT valid_migration_status
                CHECK (status IN ('SUCCESS', 'FAILED'))
            );
            """;

    public void initialize(Connection connection)
            throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_HISTORY_TABLE);
        }
    }

    public long recordMigration(
            Connection connection,
            String description,
            String sql,
            String status,
            String errorMessage
    ) throws SQLException {

        if (connection == null ||
                description == null || description.isBlank() ||
                sql == null || sql.isBlank()) {
            throw new IllegalArgumentException(
                    "Valid connection, description and SQL are required."
            );
        }

        if (!"SUCCESS".equals(status) &&
                !"FAILED".equals(status)) {
            throw new IllegalArgumentException(
                    "Status must be SUCCESS or FAILED."
            );
        }

        String query = """
                INSERT INTO migration_history
                (migration_description, migration_sql,
                 status, executed_at, error_message)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(1, description);
            statement.setString(2, sql);
            statement.setString(3, status);

            if ("SUCCESS".equals(status)) {
                statement.setTimestamp(
                        4,
                        Timestamp.valueOf(LocalDateTime.now())
                );
            } else {
                statement.setTimestamp(4, null);
            }

            statement.setString(5, errorMessage);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }
            }
        }

        throw new SQLException("Failed to record migration history.");
    }

    public boolean isAlreadyExecuted(
            Connection connection,
            String description
    ) throws SQLException {

        String query = """
                SELECT EXISTS (
                    SELECT 1
                    FROM migration_history
                    WHERE migration_description = ?
                    AND status = 'SUCCESS'
                )
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(query)) {

            statement.setString(1, description);

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    public int getTotalMigrations(
            Connection connection
    ) throws SQLException {

        String query = """
                SELECT COUNT(*) FROM migration_history
                """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query)) {

            result.next();
            return result.getInt(1);
        }
    }

    public int getSuccessfulMigrations(
            Connection connection
    ) throws SQLException {

        String query = """
                SELECT COUNT(*) FROM migration_history
                WHERE status = 'SUCCESS'
                """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query)) {

            result.next();
            return result.getInt(1);
        }
    }

    public int getFailedMigrations(
            Connection connection
    ) throws SQLException {

        String query = """
                SELECT COUNT(*) FROM migration_history
                WHERE status = 'FAILED'
                """;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query)) {

            result.next();
            return result.getInt(1);
        }
    }
}

