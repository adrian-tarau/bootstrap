package net.microfalx.bootstrap.jdbc.migration;

import net.microfalx.resource.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefinitionLoaderTest {

    @Test
    void loadSchema1() throws IOException {
        DefinitionLoader loader = new DefinitionLoader();
        loader.load(ClassPathResource.file("schema1.xml"));
        assertEquals(1, loader.getModules().size());
        Module module = loader.getModule("demo1");
        assertEquals("demo1", module.getId());
        assertEquals("Demo 1", module.getName());

        assertEquals(2, loader.getDefinitions().size());
        Definition definition = loader.getDefinition("bdc7820b16a3cb3110cde9e7fa66dbcc");
        assertEquals(2, definition.getMigrations().size());

        assertEquals("bdc7820b16a3cb3110cde9e7fa66dbcc", definition.getId());
        assertEquals("Demo 1", definition.getName());
        assertEquals("demo1", definition.getModule().getId());
        assertEquals("Demo 1", definition.getModule().getName());
    }

    @Test
    void loadAll() throws IOException {
        DefinitionLoader loader = new DefinitionLoader();
        loader.load(ClassPathResource.file("schema1.xml"));
        loader.load(ClassPathResource.file("schema2.xml"));
        assertEquals(2, loader.getModules().size());
        assertEquals(100, loader.getModule("demo1").getOrder());
        assertEquals(101, loader.getModule("demo2").getOrder());
    }
}