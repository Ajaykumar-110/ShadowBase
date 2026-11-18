
package com.shadowbase.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MigrationExecutorService {

    public void executeApprovedMigrations(
            Connection connection,
            List<MigrationPlannerService.MigrationOperation> operations,
            boolean approved
    ) throws SQLException {

        if (!approved) {
            throw new SecurityException(
                "Migration execution requires approval."
            );
        }

        if (connection == null || operations == null) {
            throw new IllegalArgumentException(
                "Connection and operations are required."
            );
        }

        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            for (var operation : operations) {

                if (operation.requiresApproval() && !approved) {
                    throw new SecurityException(
                        "Destructive migration not approved."
                    );
                }

                if (operation.sql() == null ||
                        operation.sql().isBlank()) {
                    throw new IllegalArgumentException(
                        "Migration SQL is missing."
                    );
                }

                try (Statement statement =
                        connection.createStatement()) {

                    statement.execute(operation.sql());
                }
            }

            connection.commit();

        } catch (SQLException | RuntimeException exception) {

            connection.rollback();
            throw exception;

        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }
}

