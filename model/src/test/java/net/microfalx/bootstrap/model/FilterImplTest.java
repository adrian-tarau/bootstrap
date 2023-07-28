package net.microfalx.bootstrap.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterImplTest {
    private Filter emptyFilter;
    private Filter comparisonExpressionFilter;
    private Expression comparisonExpression1;
    private Expression comparisonExpression2;
    private Expression logicalExpression1;
    private Expression logicalExpression2;
    private Filter logicalExpressionFilter;
    private Filter filterWithLimit;

    @BeforeEach
    void setUp() {
        emptyFilter = Filter.create();
        comparisonExpression1 = ComparisonExpression.like("email", "gmail");
        comparisonExpression2 = ComparisonExpression.ne("month", "March");
        logicalExpression1 = LogicalExpression.not(comparisonExpression2);
        comparisonExpressionFilter = Filter.create(comparisonExpression1);
        logicalExpressionFilter = Filter.create(logicalExpression1);
        logicalExpression2 = LogicalExpression.or(comparisonExpression1, comparisonExpression2);
        filterWithLimit = Filter.create(logicalExpression2, 0, 1);
    }

    @Test
    void getExpression() {
        assertEquals(comparisonExpression1, comparisonExpressionFilter.getExpression());
        assertEquals(logicalExpression1, logicalExpressionFilter.getExpression());
        assertEquals(logicalExpression2, filterWithLimit.getExpression());
    }

    @Test
    void getOffset() {
        assertEquals(0, emptyFilter.getOffset());
        assertEquals(0, comparisonExpressionFilter.getOffset());
        assertEquals(0, logicalExpressionFilter.getOffset());
        assertEquals(0, filterWithLimit.getOffset());
    }

    @Test
    void getLimit() {
        assertEquals(-1, emptyFilter.getLimit());
        assertEquals(-1, comparisonExpressionFilter.getLimit());
        assertEquals(-1, logicalExpressionFilter.getLimit());
        assertEquals(1, filterWithLimit.getLimit());
    }

    @Test
    void isEmpty() {
        assertTrue(emptyFilter.isEmpty());
        assertFalse(comparisonExpressionFilter.isEmpty());
        assertFalse(logicalExpressionFilter.isEmpty());
        assertFalse(filterWithLimit.isEmpty());
    }

    @Test
    void getName() {
        assertEquals("and(expr=0)", emptyFilter.getName());
        assertEquals("email", comparisonExpressionFilter.getName());
        assertEquals("not(expr=1)", logicalExpressionFilter.getName());
        assertEquals("or(expr=2)", filterWithLimit.getName());
    }

    @Test
    void getDescription() {
        assertEquals("()", emptyFilter.getDescription());
        assertEquals("email like 'gmail'", comparisonExpressionFilter.getDescription());
        assertEquals("!(month <> 'March')", logicalExpressionFilter.getDescription());
        assertEquals("(email like 'gmail' || month <> 'March')", filterWithLimit.getDescription());
    }

    @Test
    void equals() {
        assertFalse(logicalExpressionFilter.equals(filterWithLimit));
    }

    @Test
    void hashcode(){
        assertNotNull(filterWithLimit.hashCode());
    }
}