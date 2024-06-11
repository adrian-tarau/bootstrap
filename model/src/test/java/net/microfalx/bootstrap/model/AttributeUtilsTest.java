package net.microfalx.bootstrap.model;

import org.junit.jupiter.api.Test;

import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeUtilsTest {

    @Test
    void replaceVariables() {
        Attributes<Attribute> attributes = Attributes.create();
        assertEquals(null, AttributeUtils.replaceVariables(attributes, null));
        assertEquals(EMPTY_STRING, AttributeUtils.replaceVariables(attributes, EMPTY_STRING));
        assertEquals(EMPTY_STRING, AttributeUtils.replaceVariables(attributes, "${a1}"));
        attributes.add("a1", "a");
        assertEquals("a", AttributeUtils.replaceVariables(attributes, "${a1}"));
        attributes.add("a2", 1);
        assertEquals("a - 1", AttributeUtils.replaceVariables(attributes, "${a1} - ${a2}"));
        attributes.add("a3", 2);
        assertEquals("a - 12", AttributeUtils.replaceVariables(attributes, "${a1} - ${a2}${a3}"));
    }
}