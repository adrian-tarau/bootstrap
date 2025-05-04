package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public class HTMLDataSetExport<M, F extends Field<M>, ID> extends DataSetExport<M, F, ID> {

    protected HTMLDataSetExport(Format format) {
        super(format);
    }

    @Override
    protected Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> ms) {
        List<M> models = ms.orElse(Page.empty()).getContent();
        List<F> fields =  dataSet.getExportableFields();
        StringBuilder table = new StringBuilder();
        Resource resource;
        try {
            createColumns(table, fields);
            createRows(table, models, fields);
            resource = TemporaryFileResource.create(MemoryResource.create(table.toString()));
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set to HTML", e);
        }
        return resource;
    }

    private void createColumns(StringBuilder table, List<F> fields) {
        table.append("""
                <table class="table table-hover table-sm">
                  <thead class="table-dark">
                    <tr>
                """);
        fields.forEach(f -> table.append("         <th scope=\"col\">").append(f.getLabel()).append("</th>\n"));
        table.append("""
                    </tr>
                 </thead>
                """);
    }

    private void createRows(StringBuilder table, List<M> models, List<F> fields) {
        table.append("  <tbody>\n");
        try {
            for (M model : models) {
                table.append("   <tr>\n");
                String id = fields.getFirst().get(model, String.class);
                table.append("     <th scope=\"row\">").append(id).append("</th>\n");
                List<F> nonIDFields = fields.subList(1,fields.size());
                for (F field : nonIDFields) {
                    table.append("     <td>").append(StringUtils.defaultIfNull(field.get(model, String.class),
                            StringUtils.EMPTY_STRING)).append("</td>\n");
                }
                table.append("   </tr>\n");
            }
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set to CSV", e);
        }
        table.append("""
                  </tbody>
                </table>
                """);
    }
}
