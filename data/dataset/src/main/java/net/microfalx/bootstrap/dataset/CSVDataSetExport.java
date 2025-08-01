package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

class CSVDataSetExport<M, F extends Field<M>, ID> extends DataSetExport<M, F, ID> {

    protected CSVDataSetExport(Format format) {
        super(format);
    }

    @Override
    protected Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> page) {
        List<F> fields = getExportableFields();
        List<M> models = page.orElse(Page.empty()).getContent();
        CSVFormat.Builder builder = CSVFormat.DEFAULT.builder();
        String[] columns = fields.stream().map(this::getName).toList().toArray(new String[0]);
        builder.setHeader(columns);
        CSVFormat csvFormat = builder.get();
        Resource temporary = TemporaryFileResource.file("temp");
        try (final CSVPrinter printer = new CSVPrinter(temporary.getWriter(), csvFormat)) {
            for (M model : models) {
                List<String> values = fields.stream().map(f -> getValueAsString(model, f)).toList();
                printer.printRecord(values);
            }
        } catch (IOException e) {
            throw new DataSetExportException("Failed to export data set '" + dataSet.getName() + "' to CSV", e);
        }
        return temporary;
    }
}
