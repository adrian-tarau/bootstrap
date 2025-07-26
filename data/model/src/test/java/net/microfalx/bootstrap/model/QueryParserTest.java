package net.microfalx.bootstrap.model;

import com.google.common.collect.Iterators;
import jakarta.persistence.Transient;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.lang.annotation.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QueryParserTest {

    @Spy
    private I18nService i18nService = new I18nService();

    @InjectMocks
    private MetadataService metadataService;

    private QueryParser<Person, Field<Person>, String> parser;

    @BeforeEach
    void before() throws Exception {
        i18nService.afterPropertiesSet();
        //Reflect.on(metadataService).set()
        metadataService.afterPropertiesSet();
        parser = createParser(null);
    }

    @Test
    void parseEmpty() {
        LogicalExpression expression = parser.parse();
        assertEquals(0, expression.getExpressions().size());
    }

    @Test
    void parseInvalid() {
        assertFalse(createParser("firstname").isValid());
        assertEquals("An operator is expected at position 10 in expression 'firstname'", createParser("firstname").validate());
        assertFalse(createParser("firstname = ").isValid());
        assertEquals("An attribute value is expected at position 13 in expression 'firstname = '", createParser("firstname = ").validate());
        assertFalse(createParser("123").isValid());
        assertFalse(createParser("{").isValid());
        assertFalse(createParser(">=").isValid());
    }

    @Test
    void parseOneExpression() {
        parser = createParser("firstname = demo");
        LogicalExpression expression = parser.parse();
        assertEquals(LogicalExpression.Operator.AND, expression.getOperator());
        assertEquals(1, expression.getExpressions().size());
        ComparisonExpression firstExpression = (ComparisonExpression) expression.getExpressions().iterator().next();
        assertEquals("firstname", firstExpression.getField());
        assertEquals(ComparisonExpression.Operator.EQUAL, firstExpression.getOperator());
        assertEquals("demo", firstExpression.getValue());
    }

    @Test
    void parseTwoExpressionWithAnd() {
        parser = createParser("firstname = 'John Doe' and age >= 20");
        LogicalExpression expression = parser.parse();
        assertTrue(parser.isValid());
        assertEquals(LogicalExpression.Operator.AND, expression.getOperator());
        assertEquals(2, expression.getExpressions().size());

        ComparisonExpression firstExpression = (ComparisonExpression) Iterators.get(expression.getExpressions().iterator(), 0);
        assertEquals("firstname", firstExpression.getField());
        assertEquals(ComparisonExpression.Operator.EQUAL, firstExpression.getOperator());
        assertEquals("John Doe", firstExpression.getValue());

        ComparisonExpression secondExpression = (ComparisonExpression) Iterators.get(expression.getExpressions().iterator(), 1);
        assertEquals("age", secondExpression.getField());
        assertEquals(ComparisonExpression.Operator.GREATER_OR_EQUAL, secondExpression.getOperator());
        assertEquals("20", secondExpression.getValue());
    }

    @Test
    void parseWithDefaultFields() {
        parser = createParser("demo").addDefaultField("firstName").addDefaultField("lastname");
        LogicalExpression expression = parser.parse();
        assertTrue(parser.isValid());
        assertEquals(1, expression.findExpressions("firstName").size());
        assertEquals(1, expression.findExpressions("lastname").size());

        parser = createParser("demo1 and demo2").addDefaultField("firstName").addDefaultField("lastname");
        expression = parser.parse();
        assertTrue(parser.isValid());
        assertEquals(2, expression.findExpressions("firstName").size());
        assertEquals(2, expression.findExpressions("lastname").size());
    }

    @Test
    void parseWithQuotes() {
        parser = createParser("firstname = ''");
        assertTrue(parser.isValid());
        parser = createParser("firstname = 'John Doe'");
        assertTrue(parser.isValid());
        parser = createParser("firstname = \"John Doe\"");
        assertTrue(parser.isValid());
    }

    @Test
    void parseTwoExpressionWithOr() {
        parser = createParser("firstname = demo or age >= 20");
        LogicalExpression expression = parser.parse();
        assertTrue(parser.isValid());
        assertEquals(LogicalExpression.Operator.OR, expression.getOperator());
        assertEquals(2, expression.getExpressions().size());

        ComparisonExpression firstExpression = (ComparisonExpression) Iterators.get(expression.getExpressions().iterator(), 0);
        assertEquals("firstname", firstExpression.getField());
        assertEquals(ComparisonExpression.Operator.EQUAL, firstExpression.getOperator());
        assertEquals("demo", firstExpression.getValue());

        ComparisonExpression secondExpression = (ComparisonExpression) Iterators.get(expression.getExpressions().iterator(), 1);
        assertEquals("age", secondExpression.getField());
        assertEquals(ComparisonExpression.Operator.GREATER_OR_EQUAL, secondExpression.getOperator());
        assertEquals("20", secondExpression.getValue());
    }

    @Test
    void parseInfiniteLoop() {
        parser = createParser("  demo").addDefaultField("name");
        LogicalExpression expression = parser.parse();
    }

    private QueryParser<Person, Field<Person>, String> createParser(String text) {
        return new QueryParser<>(metadataService.getMetadata(Person.class), text);
    }

    public static class Person {

        @Id
        private String id;

        private String firstName;

        private String lastName;

        private String description;

        private int age;

        @Transient
        private Double dummy;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Double getDummy() {
            return dummy;
        }

        public void setDummy(Double dummy) {
            this.dummy = dummy;
        }
    }


}