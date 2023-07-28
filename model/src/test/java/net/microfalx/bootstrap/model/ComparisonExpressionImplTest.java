package net.microfalx.bootstrap.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static net.microfalx.bootstrap.model.ComparisonExpression.*;
import static org.junit.jupiter.api.Assertions.*;


class ComparisonExpressionImplTest {


    @Test
    void validateEqualsExpression() {
        ComparisonExpression equalExpression = eq("name", "test");
        assertEquals("name", equalExpression.getName());
        assertEquals("test", equalExpression.getValue());
        assertEquals(Operator.EQUAL, equalExpression.getOperator());
        assertEquals("name", equalExpression.getField());
        assertEquals("name = 'test'", equalExpression.getDescription());
        assertEquals("=", equalExpression.getOperator().getLabel());
        assertEquals(1, equalExpression.getOperator().getMinimumOperands());
        assertEquals(1, equalExpression.getOperator().getMaximumOperands());
        assertTrue(equalExpression.equals(ComparisonExpression.eq("name", "test")));
        assertFalse(equalExpression.equals(ComparisonExpression.eq("name", "test1")));
        assertNotNull(equalExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='name', " +
                "value=test, operator=Operator{minimumOperands=1, " +
                "maximumOperands=1, label='='}}", equalExpression.toString());
    }

    @Test
    void validateBetweenExpression() {
        ComparisonExpression betweenExpression = between("age", 1, 10);
        assertEquals("age", betweenExpression.getName());
        assertNotNull(betweenExpression.getValues());
        assertEquals(Operator.BETWEEN, betweenExpression.getOperator());
        assertEquals("age", betweenExpression.getField());
        assertEquals("age between [1, 10]", betweenExpression.getDescription());
        assertEquals("between", betweenExpression.getOperator().getLabel());
        assertEquals(2, betweenExpression.getOperator().getMinimumOperands());
        assertEquals(2, betweenExpression.getOperator().getMaximumOperands());
        assertFalse(betweenExpression.equals(
                ComparisonExpression.between("year", 1, 10)));
        assertNotEquals(0, betweenExpression.hashCode());
        assertNotNull(betweenExpression.toString());
    }

    @Test
    void validateNotEqualsExpression() {
        ComparisonExpression notEqualExpression = ne("year", 2000);
        assertEquals("year", notEqualExpression.getName());
        assertEquals(2000, notEqualExpression.getValue());
        assertEquals("year", notEqualExpression.getField());
        assertEquals("year <> 2000", notEqualExpression.getDescription());
        assertEquals(Operator.NOT_EQUAL, notEqualExpression.getOperator());
        assertEquals("<>", notEqualExpression.getOperator().getLabel());
        assertEquals(1, notEqualExpression.getOperator().getMinimumOperands());
        assertEquals(1, notEqualExpression.getOperator().getMaximumOperands());
        assertTrue(notEqualExpression.equals(ComparisonExpression.ne("year", 2000)));
        assertFalse(notEqualExpression.equals(ComparisonExpression.eq("year", 1900)));
        assertNotEquals(0, notEqualExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='year', value=2000, " +
                        "operator=Operator{minimumOperands=1, maximumOperands=1, label='<>'}}",
                notEqualExpression.toString());
    }

    @Test
    void validateLessExpression() {
        ComparisonExpression lessExpression = lt("speed", 100);
        assertEquals("speed", lessExpression.getName());
        assertEquals(100, lessExpression.getValue());
        assertEquals("speed", lessExpression.getField());
        assertEquals("speed < 100", lessExpression.getDescription());
        assertEquals(Operator.LESS, lessExpression.getOperator());
        assertEquals("<", lessExpression.getOperator().getLabel());
        assertEquals(1, lessExpression.getOperator().getMinimumOperands());
        assertEquals(1, lessExpression.getOperator().getMaximumOperands());
        assertTrue(lessExpression.equals(ComparisonExpression.lt("speed", 100)));
        assertFalse(lessExpression.equals(ComparisonExpression.eq("speed", 101)));
        assertNotEquals(0, lessExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='speed', " +
                        "value=100, operator=Operator{minimumOperands=1, " +
                        "maximumOperands=1, label='<'}}",
                lessExpression.toString());
    }

    @Test
    void validateGreaterExpression() {
        ComparisonExpression greaterExpression = gt("distance", 78);
        assertEquals("distance", greaterExpression.getName());
        assertEquals(78, greaterExpression.getValue());
        assertEquals("distance", greaterExpression.getField());
        assertEquals("distance > 78", greaterExpression.getDescription());
        assertEquals(Operator.GREATER, greaterExpression.getOperator());
        assertEquals(">", greaterExpression.getOperator().getLabel());
        assertEquals(1, greaterExpression.getOperator().getMinimumOperands());
        assertEquals(1, greaterExpression.getOperator().getMaximumOperands());
        assertTrue(greaterExpression.equals(ComparisonExpression.gt("distance", 78)));
        assertFalse(greaterExpression.equals(ComparisonExpression.eq("meter", 101)));
        assertNotNull(greaterExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='distance', value=78, " +
                        "operator=Operator{minimumOperands=1, maximumOperands=1, label='>'}}",
                greaterExpression.toString());
    }

    @Test
    void validateLessThanEqualsExpression() {
        ComparisonExpression lessThanEqualExpression = le("area", 17.6);
        assertEquals("area", lessThanEqualExpression.getName());
        assertEquals(17.6, lessThanEqualExpression.getValue());
        assertEquals("area", lessThanEqualExpression.getField());
        assertEquals("area <= 17.6", lessThanEqualExpression.getDescription());
        assertEquals(Operator.LESS_OR_EQUAL, lessThanEqualExpression.getOperator());
        assertEquals("<=", lessThanEqualExpression.getOperator().getLabel());
        assertEquals(1, lessThanEqualExpression.getOperator().getMinimumOperands());
        assertEquals(1, lessThanEqualExpression.getOperator().getMaximumOperands());
        assertTrue(lessThanEqualExpression.equals(ComparisonExpression.le("area", 17.6)));
        assertFalse(lessThanEqualExpression.equals(ComparisonExpression.eq("meter", 101)));
        assertNotNull(lessThanEqualExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='area', value=17.6, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='<='}}", lessThanEqualExpression.toString());
    }

    @Test
    void validateGreaterThanEqualsExpression() {
        ComparisonExpression greaterThanEqualExpression = ge("perimeter", 67);
        assertEquals("perimeter", greaterThanEqualExpression.getName());
        assertEquals(67, greaterThanEqualExpression.getValue());
        assertEquals("perimeter", greaterThanEqualExpression.getField());
        assertEquals("perimeter >= 67", greaterThanEqualExpression.getDescription());
        assertEquals(Operator.GREATER_OR_EQUAL, greaterThanEqualExpression.getOperator());
        assertEquals(">=", greaterThanEqualExpression.getOperator().getLabel());
        assertEquals(1, greaterThanEqualExpression.getOperator().getMinimumOperands());
        assertEquals(1, greaterThanEqualExpression.getOperator().getMaximumOperands());
        assertTrue(greaterThanEqualExpression.equals(ComparisonExpression.ge("perimeter", 67)));
        assertFalse(greaterThanEqualExpression.equals(ComparisonExpression.eq("perimeter", 67)));
        assertNotNull(greaterThanEqualExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='perimeter', value=67, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='>='}}", greaterThanEqualExpression.toString());
    }

    @Test
    void validateInExpression() {
        ComparisonExpression inExpression = in("collection", List.of(1, 2, 3, 4));
        assertEquals("collection", inExpression.getName());
        assertArrayEquals(List.of(1, 2, 3, 4).toArray(), inExpression.getValues());
        assertEquals("collection", inExpression.getField());
        assertEquals("collection in [1, 2, 3, 4]", inExpression.getDescription());
        assertEquals(Operator.IN, inExpression.getOperator());
        assertEquals("in", inExpression.getOperator().getLabel());
        assertEquals(1, inExpression.getOperator().getMinimumOperands());
        assertEquals(Integer.MAX_VALUE, inExpression.getOperator().getMaximumOperands());
        assertNotNull(inExpression.hashCode());
        assertNotNull(inExpression.toString());
    }

    @Test
    void validateNotInExpression() {
        ComparisonExpression notInExpression = notIn("collection", 1, 2, 4);
        assertEquals("collection", notInExpression.getName());
        assertArrayEquals(List.of(1, 2, 4).toArray(), notInExpression.getValues());
        assertEquals("collection", notInExpression.getField());
        assertEquals("collection not in [1, 2, 4]", notInExpression.getDescription());
        assertEquals(Operator.NOT_IN, notInExpression.getOperator());
        assertEquals("not in", notInExpression.getOperator().getLabel());
        assertEquals(1, notInExpression.getOperator().getMinimumOperands());
        assertEquals(Integer.MAX_VALUE, notInExpression.getOperator().getMaximumOperands());
        assertNotNull(notInExpression.hashCode());
        assertNotNull(notInExpression.toString());
    }

    @Test
    void validateNotNullExpression() {
        ComparisonExpression notNullExpression = isNotNull("apple");
        assertEquals("apple", notNullExpression.getName());
        assertEquals(null, notNullExpression.getValue());
        assertEquals("apple", notNullExpression.getField());
        assertEquals("apple not null", notNullExpression.getDescription());
        assertEquals(Operator.NOT_NULL, notNullExpression.getOperator());
        assertEquals("not null", notNullExpression.getOperator().getLabel());
        assertEquals(0, notNullExpression.getOperator().getMinimumOperands());
        assertEquals(0, notNullExpression.getOperator().getMaximumOperands());
        assertThrows(IllegalArgumentException.class, () ->
                new ComparisonExpressionImpl(Operator.NOT_NULL, "dummy", 1));
        assertTrue(notNullExpression.equals(ComparisonExpression.isNotNull("apple")));
        assertFalse(notNullExpression.equals(ComparisonExpression.eq("perimeter", 67)));
        assertNotNull(notNullExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='apple', value=null, " +
                "operator=Operator{minimumOperands=0, maximumOperands=0, " +
                "label='not null'}}", notNullExpression.toString());
    }

    @Test
    void validateNullExpression() {
        ComparisonExpression nullExpression = isNull("fruit");
        assertEquals("fruit", nullExpression.getName());
        assertNull(nullExpression.getValue());
        assertEquals("fruit", nullExpression.getField());
        assertEquals("fruit is null", nullExpression.getDescription());
        assertEquals(Operator.NULL, nullExpression.getOperator());
        assertEquals("is null", nullExpression.getOperator().getLabel());
        assertEquals(0, nullExpression.getOperator().getMinimumOperands());
        assertEquals(0, nullExpression.getOperator().getMaximumOperands());
        assertTrue(nullExpression.equals(ComparisonExpression.isNull("fruit")));
        assertFalse(nullExpression.equals(ComparisonExpression.eq("perimeter", 67)));
        assertNotNull(nullExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='fruit', value=null, " +
                "operator=Operator{minimumOperands=0, maximumOperands=0, " +
                "label='is null'}}", nullExpression.toString());
    }

    @Test
    void validateContainsExpression() {
        ComparisonExpression containsExpression = contains("rainbow", "rain");
        assertEquals("rainbow", containsExpression.getName());
        assertEquals("rain", containsExpression.getValue());
        assertEquals("rainbow", containsExpression.getField());
        assertEquals("rainbow contains 'rain'", containsExpression.getDescription());
        assertEquals(Operator.CONTAINS, containsExpression.getOperator());
        assertEquals("contains", containsExpression.getOperator().getLabel());
        assertEquals(1, containsExpression.getOperator().getMinimumOperands());
        assertEquals(1, containsExpression.getOperator().getMaximumOperands());
        assertTrue(containsExpression.equals(ComparisonExpression.contains("rainbow", "rain")));
        assertFalse(containsExpression.equals(ComparisonExpression.eq("perimeter", 67)));
        assertNotNull(containsExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='rainbow', value=rain, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='contains'}}", containsExpression.toString());
    }

    @Test
    void validateLikeExpression() {
        ComparisonExpression likeExpression = like("car", "Mazda");
        assertEquals("car", likeExpression.getName());
        assertEquals("Mazda", likeExpression.getValue());
        assertEquals("car", likeExpression.getField());
        assertEquals("car like 'Mazda'", likeExpression.getDescription());
        assertEquals(Operator.LIKE, likeExpression.getOperator());
        assertEquals("like", likeExpression.getOperator().getLabel());
        assertEquals(1, likeExpression.getOperator().getMinimumOperands());
        assertEquals(1, likeExpression.getOperator().getMaximumOperands());
        assertTrue(likeExpression.equals(ComparisonExpression.like("car", "Mazda")));
        assertFalse(likeExpression.equals(ComparisonExpression.eq("perimeter", 67)));
        assertNotNull(likeExpression.hashCode());
        assertEquals("ComparisonExpressionImpl{field='car', value=Mazda, " +
                "operator=Operator{minimumOperands=1, maximumOperands=1, " +
                "label='like'}}", likeExpression.toString());
    }
}