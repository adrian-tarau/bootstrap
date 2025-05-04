package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.dataset.model.Person;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HTMLDataSetExportTest extends AbstractDataSetTestCase {

    @Test
    void export() throws IOException {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class);
        DataSetExport<Person, Field<Person>, Integer> dataSetExport =
                DataSetExport.create(DataSetExport.Format.HTML);
        assertNotNull(dataSetExport);
        Resource resource = dataSetExport.export(dataSet);
        assertNotNull(resource);
        Assertions.assertThat(resource.loadAsString()).contains(List.of(
                "<table class=\"table table-hover table-sm\">",
                "<thead class=\"table-dark\">", "<tr>", "<th scope=\"col\">", "</th>", "</tr>", "</thead>", "<tbody>",
                "<th scope=\"row\">", "</th>", "</tbody>", "</table>"));

    }
}