
package com.shadowbase;

import com.shadowbase.service.MigrationPlannerService;
import com.shadowbase.service.SchemaComparisonService.DifferenceType;
import com.shadowbase.service.SchemaComparisonService.SchemaDifference;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MigrationPlannerServiceTest {

    @Test
    void shouldGenerateSafeMigrationPlan() {

        MigrationPlannerService planner =
                new MigrationPlannerService();

        List<SchemaDifference> differences = List.of(

                new SchemaDifference(
                        DifferenceType.MISSING_COLUMN_IN_TARGET,
                        "employees",
                        "department",
                        "varchar",
                        null
                ),

                new SchemaDifference(
                        DifferenceType.EXTRA_COLUMN_IN_TARGET,
                        "employees",
                        "email",
                        null,
                        "varchar"
                )
        );

        var plan = planner.generatePlan(differences);

        assertEquals(2, plan.size());

        assertEquals(
                "ALTER TABLE \"employees\" ADD COLUMN \"department\" VARCHAR;",
                plan.get(0).sql()
        );

        assertEquals(
                MigrationPlannerService.RiskLevel.LOW,
                plan.get(0).risk()
        );

        assertFalse(plan.get(0).requiresApproval());

        assertEquals(
                MigrationPlannerService.RiskLevel.HIGH,
                plan.get(1).risk()
        );

        assertTrue(plan.get(1).requiresApproval());

        plan.forEach(System.out::println);

        System.out.println("Migration planner test passed!");
    }

    @Test
    void shouldRejectUnsupportedSqlType() {

        MigrationPlannerService planner =
                new MigrationPlannerService();

        List<SchemaDifference> differences = List.of(
                new SchemaDifference(
                        DifferenceType.MISSING_COLUMN_IN_TARGET,
                        "employees",
                        "department",
                        "unknown_type",
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> planner.generatePlan(differences)
        );
    }
}

