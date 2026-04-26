package net.microfalx.bootstrap.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest extends AbstractConfigurationTestCase {

    private Configuration configuration;

    @Override
    protected void postSetup() {
        super.postSetup();
        setupEnvironment();
        mockRegistry();
        configuration = new ConfigurationImpl(configurationService);
    }

    @Test
    void get() {
        assertEquals("", configuration.get("x"));
        assertEquals("1", configuration.get("a"));
    }

    @Test
    void getAsBoolean() {
        assertEquals(true, configuration.get("x", true));
        assertEquals(true, configuration.get("c", false));
    }

    @Test
    void getAsInt() {
        assertEquals(2, configuration.get("x", 2));
        assertEquals(1, configuration.get("a", 2));
    }

    @Test
    void getAsLong() {
        assertEquals(2, configuration.get("x", 2L));
        assertEquals(1, configuration.get("a", 2L));
    }

    @Test
    void getWithType() {
        assertEquals(5, configuration.get("x", Integer.class, 5));
        assertEquals(1L, configuration.get("a", Long.class, 2L));
    }

    @Test
    void set() {
        assertEquals("", configuration.get("x"));
        configuration.set("a", 1);
        assertEquals("1", configuration.get("a"));
    }

    private void setupEnvironment() {
        environment.setProperty("a", 1);
        environment.setProperty("b", 1L);
        environment.setProperty("c", true);
    }
}
