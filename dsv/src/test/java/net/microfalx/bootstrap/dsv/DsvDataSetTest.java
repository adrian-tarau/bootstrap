package net.microfalx.bootstrap.dsv;

import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DsvDataSetTest {

    @Test
    void countRecords() throws IOException {
        Resource resource = ClassPathResource.file("test1.csv");
        DsvDataSet dataSet = DsvDataSet.create(resource);
        assertEquals(2, dataSet.findAll().size());
    }

    @Test
    void accessFields() throws IOException {
        Resource resource = ClassPathResource.file("test1.csv");
        DsvDataSet dataSet = DsvDataSet.create(resource);
        Iterator<DsvRecord> iterator = dataSet.findAll().iterator();
        DsvRecord record = iterator.next();

        assertEquals("1", record.get(0));
        assertEquals("1", record.get("a"));

        assertEquals("2", record.get(1));
        assertEquals("2", record.get("b"));
    }

}