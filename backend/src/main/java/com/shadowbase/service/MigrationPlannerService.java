
package com.shadowbase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.shadowbase.service.SchemaComparisonService.SchemaDifference;

public class MigrationPlannerService {

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    public record MigrationOperation(
            String description,
            String sql,
            RiskLevel risk,
            boolean requiresApproval
    ) {}

    private static final Set<String> SAFE_TYPES = Set.of(
            "int2", "int4", "int8",
            "smallint", "integer", "bigint",
            "varchar", "bpchar", "text",
            "bool", "boolean",
            "numeric", "float4", "float8",
            "date", "timestamp", "timestamptz",
            "uuid"
    );

    public List<MigrationOperation> generatePlan(
            List<SchemaDifference> differences
    ) {

        List<MigrationOperation> operations = new ArrayList<>();

        for (SchemaDifference difference : differences) {

            String table = quoteIdentifier(difference.tableName());
            String column = difference.columnName() == null
                    ? null
                    : quoteIdentifier(difference.columnName());

            switch (difference.type()) {

                case MISSING_COLUMN_IN_TARGET -> {
                    String type = normalizeType(difference.sourceValue());

                    operations.add(new MigrationOperation(
                            "Add missing column " + difference.columnName(),
                            "ALTER TABLE " + table +
                            " ADD COLUMN " + column + " " + type + ";",
                            RiskLevel.LOW,
                            false
                    ));
                }

                case EXTRA_COLUMN_IN_TARGET -> operations.add(
                        new MigrationOperation(
                                "Remove extra column " + difference.columnName(),
                                "ALTER TABLE " + table +
                                " DROP COLUMN " + column + ";",
                                RiskLevel.HIGH,
                                true
                        )
                );

                case MISSING_TABLE_IN_TARGET -> operations.add(
                        new MigrationOperation(
                                "Create missing table " + difference.tableName(),
                                null,
                                RiskLevel.MEDIUM,
                                true
                        )
                );

                case EXTRA_TABLE_IN_TARGET -> operations.add(
                        new MigrationOperation(
                                "Remove extra table " + difference.tableName(),
                                "DROP TABLE " + table + ";",
                                RiskLevel.HIGH,
                                true
                        )
                );

                case COLUMN_TYPE_MISMATCH -> operations.add(
                        new MigrationOperation(
                                "Review type mismatch in " +
                                difference.tableName() + "." +
                                difference.columnName(),
                                null,
                                RiskLevel.HIGH,
                                true
                        )
                );
            }
        }

        return operations;
    }

    private String normalizeType(String type) {

        if (type == null || !SAFE_TYPES.contains(
                type.trim().toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported database type: " + type
            );
        }

        return type.trim().toUpperCase();
    }

    private String quoteIdentifier(String identifier) {

        if (identifier == null ||
                !identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Invalid SQL identifier: " + identifier
            );
        }

        return "\"" + identifier + "\"";
    }
}

