package net.microfalx.bootstrap.web.util;

import net.microfalx.resource.ClassPathResource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TableGeneratorTest {

    @Test
    void simple() {
        TableGenerator generator = new TableGenerator().addColumns("First Name", "Last Name", "Age", "Retired")
                .addRow("John", "Doe", 70, true)
                .addRow("John2", "Doe2", 24, false);
        Assertions.assertThat(generator.generate())
                .contains("<table").contains("</table>")
                .contains("<tr>").contains("</tr>")
                .contains("table table-striped table-bordered table-hover");
    }

    @Test
    void csv() throws IOException {
        TableGenerator generator = new TableGenerator().addRows(ClassPathResource.file("util/people.csv"));
        Assertions.assertThat(generator.generate())
                .contains("<table").contains("</table>")
                .contains("<tr>").contains("</tr>")
                .contains("table table-striped table-bordered table-hover");
    }

}