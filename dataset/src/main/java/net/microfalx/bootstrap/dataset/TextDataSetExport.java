package net.microfalx.bootstrap.dataset;

import com.jakewharton.fliptables.FlipTable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class TextDataSetExport<M, F extends Field<M>, ID> extends DataSetExport<M,F,ID> {

    protected TextDataSetExport(Format format) {
        super(format);
    }

    @Override
    protected Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> ms) {
        List<M> models = ms.orElse(Page.empty()).getContent();
        List<F> fields = dataSet.getExportableFields();
        List<String> headers = fields.stream().map(Field::getName).toList();
        List<String [] > rows= new ArrayList<>();
        Resource resource;
        try {
            for (M model:models){
                List<String> row= new ArrayList<>();
                for (F field:fields){
                    row.add(StringUtils.defaultIfNull(field.get(model, String.class),StringUtils.EMPTY_STRING));
                }
                String[] array = row.toArray(new String[0]);
                rows.add(array);
            }
            String[][] data = rows.toArray(new String[rows.size()][headers.size()]);
            String table = FlipTable.of(headers.toArray(new String[0]), data);
            resource=TemporaryFileResource.create(MemoryResource.create(table));
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set '" + dataSet.getName() + "' to text", e);
        }
        return resource;
    }
}
