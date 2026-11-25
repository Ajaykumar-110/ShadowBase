
package com.shadowbase.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class MigrationExecutorService {

    private final MigrationHistoryService historyService;

    public MigrationExecutorService(
            MigrationHistoryService historyService) {

        this.historyService = historyService;
    }

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

        if (connection == null ||
                operations == null ||
                operations.isEmpty()) {

            throw new IllegalArgumentException(
                    "Connection and migration operations are required."
            );
        }

        for (var operation : operations) {

            if (operation == null ||
                    operation.description() == null ||
                    operation.description().isBlank() ||
                    operation.sql() == null ||
                    operation.sql().isBlank()) {

                throw new IllegalArgumentException(
                        "Valid migration description and SQL are required."
                );
            }

            if (operation.requiresApproval() && !approved) {
                throw new SecurityException(
                        "Destructive migration not approved."
                );
            }
        }

        boolean originalAutoCommit = connection.getAutoCommit();

        MigrationPlannerService.MigrationOperation currentOperation = null;

        try {

            historyService.initialize(connection);

            connection.setAutoCommit(false);

            for (var operation : operations) {

                currentOperation = operation;

                try (Statement statement =
                             connection.createStatement()) {

                    statement.execute(operation.sql());
                }

                historyService.recordMigration(
                        connection,
                        operation.description(),
                        operation.sql(),
                        "SUCCESS",
                        null
                );
            }

            connection.commit();

        } catch (SQLException | RuntimeException exception) {

            try {

                connection.rollback();

                if (currentOperation != null) {

                    historyService.recordMigration(
                            connection,
                            currentOperation.description(),
                            currentOperation.sql(),
                            "FAILED",
                            exception.getMessage()
                    );

                    connection.commit();
                }

            } catch (SQLException historyException) {

                exception.addSuppressed(historyException);
            }

            throw exception;

        } finally {

            connection.setAutoCommit(originalAutoCommit);
        }
    }
}
