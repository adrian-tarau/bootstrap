package net.microfalx.bootstrap.dos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DosRegistryTest extends AbstractDosTestCase {

    private DosRegistry registry;

    @BeforeEach
    void setup() throws Exception {
        super.setup();
        registry = new DosRegistry();
        registry.initialize(persistence, properties, threadPool);
    }

    @Test
    void registerRule() {
        assertEquals(0, registry.getRules().size());
        registry.register(Rule.create("localhost", Rule.Type.IP).build());
        assertEquals(1, registry.getRules().size());
    }

    @Test
    void findRule() {
        assertNull(registry.findRule("localhost"));
        registry.register(Rule.create("localhost", Rule.Type.IP).build());
        assertNotNull(registry.findRule("localhost"));
    }
}