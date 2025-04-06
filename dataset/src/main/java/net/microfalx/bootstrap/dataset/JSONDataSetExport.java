package net.microfalx.bootstrap.dataset;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public class JSONDataSetExport<M, F extends Field<M>, ID> extends DataSetExport<M, F, ID> {
    protected JSONDataSetExport(Format format) {
        super(format);
    }

    @Override
    protected Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> page) {
        List<F> fields = getExportableFields();
        List<M> models = page.orElse(Page.empty()).getContent();
        Resource resource = TemporaryFileResource.file("temp", "json");
        try {
            ObjectMapper objectMapper = new JsonMapper();
            ObjectNode root = objectMapper.createObjectNode();
            extractMetadata(fields, root);
            extractData(root, models, fields);

            DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
            ObjectWriter writer = objectMapper.writer(printer);
            writer.writeValue(resource.getWriter(), root);
            return resource;
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set to JSON", e);
        }
    }

    private void extractMetadata(List<F> fields, ObjectNode root) {
        ArrayNode fieldNodes = root.withArrayProperty("fields");
        for (F field : fields) {
            ObjectNode objectNode = fieldNodes.addObject();
            objectNode.put("name", field.getName());
            objectNode.put("label", field.getLabel());
            objectNode.put("data-type", field.getDataType().name());
            objectNode.put("required", field.isRequired());
            objectNode.put("id", field.isId());
        }
    }

    private void extractData(ObjectNode root, List<M> models, List<F> fields) {
        ArrayNode dataArray = root.withArrayProperty("data");
        for (M model : models) {
            ArrayNode modelArray = dataArray.addArray();
            for (F field : fields) {
                modelArray.add(getValueAsString(model, field));
            }
        }
    }


}
