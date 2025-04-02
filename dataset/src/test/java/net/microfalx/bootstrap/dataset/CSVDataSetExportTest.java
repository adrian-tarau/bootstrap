package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.dataset.model.Person;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CSVDataSetExportTest extends AbstractDataSetTestCase {

    @Test
    void export() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport =
                DataSetExport.create(DataSetExport.Format.CSV);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertNotNull(resource);
        Assertions.assertThat(resource.loadAsString()).
                isEqualToIgnoringNewLines("age,description,dummy,firstName,id,lastName\n" +
                        "25,,,John,1,Doe\n" +
                        "20,,,Jane,1,Doe\n");
    }


}