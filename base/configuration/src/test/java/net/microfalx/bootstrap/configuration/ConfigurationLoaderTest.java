package net.microfalx.bootstrap.configuration;

import net.microfalx.resource.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationLoaderTest {

    @Test
    void loadCount() throws IOException {
        ConfigurationLoader loader = load();
        assertEquals(6, loader.getMetadata().size());
        assertEquals(3, loader.getGroupCount());
        assertEquals(3, loader.getItemCount());
    }

    @Test
    void loadGroupsFirstLevel() throws IOException {
        ConfigurationLoader loader = load();
        Metadata metadata = loader.getMetadata().get("group1");
        assertNotNull(metadata);
        assertEquals("group1", metadata.getFullKey());
        assertEquals("Group 1", metadata.getName());
        assertEquals(null, metadata.getDescription());
        assertFalse(metadata.isLeaf());
        assertEquals(0, metadata.getOrder());
        assertEquals(2, metadata.getChildren().size());

        metadata = loader.getMetadata().get("group2");
        assertNotNull(metadata);
        assertEquals(1, metadata.getOrder());
    }

    @Test
    void loadGroupSecondLevel() throws IOException {
        ConfigurationLoader loader = load();
        Metadata metadata = loader.getMetadata().get("group1");
        assertNotNull(metadata);
        metadata = loader.getMetadata().get("group1_group12");
        assertNotNull(metadata);
        assertEquals("group1.group12", metadata.getFullKey());
        assertEquals("Group 1-2", metadata.getName());
        assertEquals("Group 1 description", metadata.getDescription());
        assertFalse(metadata.isLeaf());
        assertEquals(0, metadata.getOrder());
        assertEquals(2, metadata.getChildren().size());
    }

    @Test
    void loadItemInteger() throws IOException {
        ConfigurationLoader loader = load();
        Metadata metadata = loader.getMetadata().get("group1_item1");
        assertNotNull(metadata);
        assertEquals("group1.item1", metadata.getFullKey());
        assertEquals("Item 1", metadata.getName());
        assertEquals(Metadata.DataType.INTEGER, metadata.getDataType());
        assertEquals("Item 1 description", metadata.getDescription());
        assertEquals(1, metadata.getOrder());
        assertEquals(0L, metadata.getMinimum());
        assertEquals(100L, metadata.getMaximum());
    }

    @Test
    void loadItemNumber() throws IOException {
        ConfigurationLoader loader = load();
        Metadata metadata = loader.getMetadata().get("group1_group12_item2");
        assertNotNull(metadata);
        assertEquals("group1.group12.item2", metadata.getFullKey());
        assertEquals("Item 2", metadata.getName());
        assertEquals(Metadata.DataType.NUMBER, metadata.getDataType());
        assertEquals("Item 2 description", metadata.getDescription());
        assertEquals(1, metadata.getOrder());
        assertEquals(1.5d, metadata.getMinimum());
        assertEquals(5.5d, metadata.getMaximum());
    }

    private ConfigurationLoader load() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader();
        loader.load(ClassPathResource.file("configuration.xml"));
        return loader;
    }


}