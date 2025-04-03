package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.dataset.model.Person;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class XMLDataSetExportTest extends AbstractDataSetTestCase {

    @Test
    void export() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport =
                DataSetExport.create(DataSetExport.Format.XML);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertNotNull(resource);
        Assertions.assertThat(resource.loadAsString()).contains(List.of("<fields>","</fields>","field",
                 "<dataset>","</dataset>","<model>","</model>","<data>","</data>","<value>","</value>"));

    }
}