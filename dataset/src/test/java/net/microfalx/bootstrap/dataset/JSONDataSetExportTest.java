package net.microfalx.bootstrap.dataset;

import com.google.common.collect.Iterators;
import net.microfalx.bootstrap.dataset.model.Person;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JSONDataSetExportTest extends AbstractDataSetTestCase {

    @Test
    void export() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport = DataSetExport.create(DataSetExport.Format.JSON);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertResource(resource);
        Assertions.assertThat(resource.loadAsString()).contains(List.of("fields", "data","name","label","data-type",
                "required","id"));
    }

    @Test
    void exportSchema() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport = DataSetExport.<Person, Field<Person>, Integer>create(DataSetExport.Format.JSON)
                .setIncludeData(false).setIncludeAll(true);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertResource(resource);
        Assertions.assertThat(resource.loadAsString()).contains("$schema", "properties", "title",
                "required", "id").doesNotContain("data");
    }

    @Test
    void exportData() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport = DataSetExport.<Person, Field<Person>, Integer>create(DataSetExport.Format.JSON)
                .setIncludeMetadata(false).setIncludeAll(true);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertResource(resource);
        Assertions.assertThat(resource.loadAsString()).contains("firstName", "lastName").doesNotContain("data");
    }

    @Test
    void exportMultiple() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport = DataSetExport.<Person, Field<Person>, Integer>create(DataSetExport.Format.JSON)
                .setMultipleFiles(true);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertResource(resource);
        try (ZipFile zipFile = new ZipFile(ResourceUtils.toFile(resource))) {
            assertEquals(2, Iterators.size(Iterators.forEnumeration(zipFile.entries())));
        }
    }

    private void assertResource(Resource resource) throws IOException {
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.length() > 100);
        assertTrue(resource.isReadable());
        assertTrue(resource.isWritable());
    }

    @Provider
    public static class JsonDataSetExportCallback implements DataSetExportCallback<Person, Field<Person>, Integer> {

        @Override
        public String getFieldName(DataSet<Person, Field<Person>, Integer> dataSet, Field<Person> field) {
            if ("renamed".equalsIgnoreCase(field.getName())) {
                return "renamed2";
            } else {
                return DataSetExportCallback.super.getFieldName(dataSet, field);
            }
        }

        @Override
        public boolean isExportable(DataSet<Person, Field<Person>, Integer> dataSet, Field<Person> field, boolean exportable) {
            return !"nonExportable".equalsIgnoreCase(field.getName());
        }
    }
}