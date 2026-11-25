
package com.shadowbase.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Service
public class SchemaComparisonService {

    public enum DifferenceType {
        MISSING_TABLE_IN_TARGET,
        EXTRA_TABLE_IN_TARGET,
        MISSING_COLUMN_IN_TARGET,
        EXTRA_COLUMN_IN_TARGET,
        COLUMN_TYPE_MISMATCH
    }

    public record SchemaDifference(
            DifferenceType type,
            String tableName,
            String columnName,
            String sourceValue,
            String targetValue
    ) {}

    private record ColumnInfo(
            String typeName,
            int jdbcType
    ) {}

    public List<SchemaDifference> compare(
            Connection source,
            Connection target
    ) throws SQLException {

        if (source == null || target == null) {
            throw new IllegalArgumentException(
                    "Source and target connections are required."
            );
        }

        Map<String, Map<String, ColumnInfo>> sourceSchema =
                inspect(source);

        Map<String, Map<String, ColumnInfo>> targetSchema =
                inspect(target);

        List<SchemaDifference> differences = new ArrayList<>();

        Set<String> allTables = new TreeSet<>();
        allTables.addAll(sourceSchema.keySet());
        allTables.addAll(targetSchema.keySet());

        for (String table : allTables) {

            Map<String, ColumnInfo> sourceColumns =
                    sourceSchema.get(table);

            Map<String, ColumnInfo> targetColumns =
                    targetSchema.get(table);

            if (sourceColumns == null) {
                differences.add(new SchemaDifference(
                        DifferenceType.EXTRA_TABLE_IN_TARGET,
                        table, null, null, "Present"
                ));
                continue;
            }

            if (targetColumns == null) {
                differences.add(new SchemaDifference(
                        DifferenceType.MISSING_TABLE_IN_TARGET,
                        table, null, "Present", null
                ));
                continue;
            }

            Set<String> allColumns = new TreeSet<>();
            allColumns.addAll(sourceColumns.keySet());
            allColumns.addAll(targetColumns.keySet());

            for (String column : allColumns) {

                ColumnInfo sourceInfo = sourceColumns.get(column);
                ColumnInfo targetInfo = targetColumns.get(column);

                if (sourceInfo == null) {
                    differences.add(new SchemaDifference(
                            DifferenceType.EXTRA_COLUMN_IN_TARGET,
                            table,
                            column,
                            null,
                            targetInfo.typeName()
                    ));
                    continue;
                }

                if (targetInfo == null) {
                    differences.add(new SchemaDifference(
                            DifferenceType.MISSING_COLUMN_IN_TARGET,
                            table,
                            column,
                            sourceInfo.typeName(),
                            null
                    ));
                    continue;
                }

                if (sourceInfo.jdbcType() != targetInfo.jdbcType()) {
                    differences.add(new SchemaDifference(
                            DifferenceType.COLUMN_TYPE_MISMATCH,
                            table,
                            column,
                            sourceInfo.typeName(),
                            targetInfo.typeName()
                    ));
                }
            }
        }

        return differences;
    }

    private Map<String, Map<String, ColumnInfo>> inspect(
            Connection connection
    ) throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Database connection cannot be null."
            );
        }

        Map<String, Map<String, ColumnInfo>> schema =
                new TreeMap<>();

        DatabaseMetaData metadata = connection.getMetaData();

        String catalog = connection.getCatalog();
        String schemaName = connection.getSchema();

        try (ResultSet tables = metadata.getTables(
                catalog,
                schemaName,
                "%",
                new String[]{"TABLE"}
        )) {

            while (tables.next()) {

                String tableName = tables.getString("TABLE_NAME");

                Map<String, ColumnInfo> columns = new TreeMap<>();

                try (ResultSet columnResults = metadata.getColumns(
                        catalog,
                        schemaName,
                        tableName,
                        "%"
                )) {

                    while (columnResults.next()) {

                        String columnName =
                                columnResults.getString("COLUMN_NAME");

                        String typeName =
                                columnResults.getString("TYPE_NAME");

                        int jdbcType =
                                columnResults.getInt("DATA_TYPE");

                        columns.put(
                                columnName,
                                new ColumnInfo(typeName, jdbcType)
                        );
                    }
                }

                schema.put(tableName, columns);
            }
        }

        return schema;
    }
}
