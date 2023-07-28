package net.microfalx.bootstrap.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static net.microfalx.bootstrap.model.ComparisonExpression.eq;
import static net.microfalx.bootstrap.model.ComparisonExpression.ne;
import static net.microfalx.bootstrap.model.LogicalExpression.and;
import static net.microfalx.bootstrap.model.LogicalExpression.or;
import static org.junit.jupiter.api.Assertions.*;

class LogicalExpressionImplTest {

    ComparisonExpression notEqualExpression;
    ComparisonExpression equalExpression;

    @BeforeEach
    void setUp() {
        notEqualExpression = ne("age", 10);
        equalExpression = eq("month", "July");
    }

    @Test
    void validateNotExpression() {
        LogicalExpression not = LogicalExpression.not(equalExpression);
        assertIterableEquals(List.of(equalExpression), not.getExpressions());
        assertEquals(LogicalExpression.Operator.NOT, not.getOperator());
        assertEquals("not(expr=1)", not.getName());
        assertEquals("!(month = 'July')", not.getDescription());
        assertNotNull(not.hashCode());
        assertFalse(not.equals(LogicalExpression.not(notEqualExpression)));
        assertEquals("LogicalExpressionImpl{expressions=" +
                "[ComparisonExpressionImpl{field='month', value=July, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='='}}], operator=NOT}",not.toString());
    }

    @Test
    void validateOrExpression() {
        LogicalExpression or1 = or(notEqualExpression);
        assertEquals(LogicalExpression.Operator.OR, or1.getOperator());
        assertIterableEquals(Collections.singletonList(notEqualExpression), or1.getExpressions());
        assertEquals("(age <> 10)", or1.getDescription());
        assertEquals("or(expr=1)", or1.getName());
        LogicalExpression or2 = or(List.of(notEqualExpression, equalExpression));
        assertEquals(LogicalExpression.Operator.OR, or2.getOperator());
        assertIterableEquals(List.of(notEqualExpression, equalExpression), or2.getExpressions());
        assertEquals("(age <> 10 || month = 'July')", or2.getDescription());
        assertEquals("or(expr=2)", or2.getName());
        assertFalse(or1.equals(or2));
        assertNotNull(or1.hashCode());
        assertEquals("LogicalExpressionImpl{expressions=" +
                "[ComparisonExpressionImpl{field='age', value=10, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='<>'}}], operator=OR}",or1.toString());
    }

    @Test
    void validateAndExpression() {
        LogicalExpression and1 = and(notEqualExpression);
        assertEquals(LogicalExpression.Operator.AND, and1.getOperator());
        assertIterableEquals(Collections.singletonList(notEqualExpression), and1.getExpressions());
        assertEquals("(age <> 10)", and1.getDescription());
        assertEquals("and(expr=1)", and1.getName());
        LogicalExpression and2 = and(List.of(notEqualExpression, equalExpression));
        assertEquals(LogicalExpression.Operator.AND, and2.getOperator());
        assertIterableEquals(List.of(notEqualExpression, equalExpression), and2.getExpressions());
        assertEquals("(age <> 10 && month = 'July')", and2.getDescription());
        assertEquals("and(expr=2)", and2.getName());
        assertFalse(and1.equals(and2));
        assertNotNull(and1.hashCode());
        assertEquals("LogicalExpressionImpl{expressions=" +
                "[ComparisonExpressionImpl{field='age', value=10, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='<>'}}], operator=AND}",and1.toString());
    }
}