package net.microfalx.bootstrap.model;

import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonTest {

    @Test
    void resource() throws IOException {
        String json = Types.asString(ClassPathResource.file("i18n_en.properties"));
        Resource resource = Types.asObject(json, Resource.class);
        assertEquals("i18n_en.properties", resource.getFileName());
        assertEquals(1687262677697L, resource.lastModified());
        assertEquals(286L, resource.length());
    }

}