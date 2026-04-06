package net.microfalx.bootstrap.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataImplTest {

    private NodeImpl testNode;
    private DataImpl dataImpl;

    @BeforeEach
    void setUp() {
        testNode = new NodeImpl(null, "/test/path");
    }

    @Test
    void constructorWithExists() {
        dataImpl = new DataImpl(testNode, true);
        assertTrue(dataImpl.exists());
        assertEquals(testNode, dataImpl.getNode());
    }

    @Test
    void constructorWithNotExists() {
        dataImpl = new DataImpl(testNode, false);
        assertFalse(dataImpl.exists());
        assertEquals(testNode, dataImpl.getNode());
    }

    @Test
    void constructorWithNullNode() {
        assertThrows(IllegalArgumentException.class, () -> new DataImpl(null, true));
    }

    @Test
    void constructorWithAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", 42);

        dataImpl = new DataImpl(testNode, attributes);
        assertTrue(dataImpl.exists());
        assertEquals(testNode, dataImpl.getNode());
        assertEquals("value1", dataImpl.getAttribute("key1"));
        assertEquals(42, dataImpl.getAttribute("key2", 2));
    }

    @Test
    void getNode() {
        dataImpl = new DataImpl(testNode, true);
        assertSame(testNode, dataImpl.getNode());
    }

    @Test
    void testExists() {
        dataImpl = new DataImpl(testNode, true);
        assertTrue(dataImpl.exists());

        dataImpl = new DataImpl(testNode, false);
        assertFalse(dataImpl.exists());
    }

    @Test
    void setAndGet() {
        dataImpl = new DataImpl(testNode, true);
        String testValue = "test string";
        dataImpl.set(testValue);
        assertEquals(testValue, dataImpl.get());
    }

    @Test
    void setNull() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.set("initial value");
        dataImpl.set(null);
        assertNull(dataImpl.get());
    }

    @Test
    void setStoresDataType() {
        dataImpl = new DataImpl(testNode, true);
        Integer testValue = 123;
        dataImpl.set(testValue);
        assertEquals(Integer.class.getName(), dataImpl.getAttribute("$DATA_CLASS$"));
    }

    @Test
    void setNullRemovesDataType() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.set("value");
        assertNotNull(dataImpl.getAttribute("$DATA_CLASS$"));
        dataImpl.set(null);
        assertNull(dataImpl.getAttribute("$DATA_CLASS$"));
    }

    @Test
    void setAttributeWithEmptyName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.setAttribute("", "value"));
    }

    @Test
    void setAttributeWithNullName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.setAttribute(null, "value"));
    }

    @Test
    void setAttribute() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("customKey", "customValue");
        assertEquals("customValue", dataImpl.getAttribute("customKey"));
    }

    @Test
    void getAttributeWithEmptyName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute(""));
    }

    @Test
    void getAttributeWithNullName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute((String) null));
    }

    @Test
    void getAttributeNonExistent() {
        dataImpl = new DataImpl(testNode, true);
        assertNull(dataImpl.getAttribute("nonExistent"));
    }

    @Test
    void getAttributeStringWithDefault() {
        dataImpl = new DataImpl(testNode, true);
        String result = dataImpl.getAttribute("missing", "defaultValue");
        assertEquals("defaultValue", result);
    }

    @Test
    void getAttributeStringWithValue() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", "actualValue");
        String result = dataImpl.getAttribute("key", "defaultValue");
        assertEquals("actualValue", result);
    }

    @Test
    void getAttributeStringWithEmptyName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute("", "default"));
    }

    @Test
    void getAttributeIntWithDefault() {
        dataImpl = new DataImpl(testNode, true);
        int result = dataImpl.getAttribute("missing", 42);
        assertEquals(42, result);
    }

    @Test
    void getAttributeIntFromNumber() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", 100L);
        int result = dataImpl.getAttribute("key", 42);
        assertEquals(100, result);
    }

    @Test
    void getAttributeIntFromString() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", "200");
        int result = dataImpl.getAttribute("key", 42);
        assertEquals(200, result);
    }

    @Test
    void getAttributeIntInvalidConversion() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", new Object());
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute("key", 42));
    }

    @Test
    void getAttributeIntWithEmptyName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute("", 42));
    }

    @Test
    void getAttributeLongWithDefault() {
        dataImpl = new DataImpl(testNode, true);
        long result = dataImpl.getAttribute("missing", 42L);
        assertEquals(42L, result);
    }

    @Test
    void getAttributeLongFromNumber() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", 100);
        long result = dataImpl.getAttribute("key", 42L);
        assertEquals(100L, result);
    }

    @Test
    void getAttributeLongFromString() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", "200");
        long result = dataImpl.getAttribute("key", 42L);
        assertEquals(200L, result);
    }

    @Test
    void getAttributeLongInvalidConversion() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", new Object());
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute("key", 42L));
    }

    @Test
    void getAttributeLongWithEmptyName() {
        dataImpl = new DataImpl(testNode, true);
        assertThrows(IllegalArgumentException.class, () -> dataImpl.getAttribute("", 42L));
    }

    @Test
    void multipleAttributes() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("attr1", "value1");
        dataImpl.setAttribute("attr2", 123);
        dataImpl.setAttribute("attr3", 1234L);
        dataImpl.setAttribute("attr4", true);

        assertEquals("value1", dataImpl.getAttribute("attr1"));
        assertEquals(123, dataImpl.getAttribute("attr2", 1));
        assertEquals(1234L, dataImpl.getAttribute("attr3", 1L));
        assertEquals(true, dataImpl.getAttribute("attr4"));
    }

    @Test
    void setAttributeOverwrite() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("key", "value1");
        assertEquals("value1", dataImpl.getAttribute("key"));

        dataImpl.setAttribute("key", "value2");
        assertEquals("value2", dataImpl.getAttribute("key"));
    }

    @Test
    void setPreservesAttributes() {
        dataImpl = new DataImpl(testNode, true);
        dataImpl.setAttribute("customAttr", "customValue");
        dataImpl.set("dataValue");

        assertEquals("dataValue", dataImpl.get());
        assertEquals("customValue", dataImpl.getAttribute("customAttr"));
    }

}