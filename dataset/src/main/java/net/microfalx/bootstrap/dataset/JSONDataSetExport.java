package net.microfalx.bootstrap.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
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
        Metadata<M, F, ID> metadata = dataSet.getMetadata();
        List<F> fields = metadata.getFields();
        List<M> models = page.orElse(Page.empty()).getContent();
        Resource resource = TemporaryFileResource.file("temp", "json");
        ObjectMapper objectMapper = new JsonMapper();
        JsonNode metadataNode=extractMetadata(fields, objectMapper);
        extractData(objectMapper, metadataNode,models, fields, resource);
        return resource;
    }

    private void extractData(ObjectMapper objectMapper,JsonNode metadataNode, List<M> models, List<F> fields, Resource resource) {
        try {
            ArrayNode data=objectMapper.createArrayNode();
            for (M model : models) {
                ArrayNode dataArray = objectMapper.createArrayNode();
                for (F field : fields) {
                    dataArray.add(field.get(model, String.class));
                }
                data.add(dataArray);
            }
            ObjectNode objectNode= objectMapper.createObjectNode();
            objectNode.set("data",data);
            ((ObjectNode) metadataNode).setAll(objectNode);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(resource.getWriter(),metadataNode);
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set to JSON", e);
        }
    }

    private JsonNode extractMetadata(List<F> fields, ObjectMapper objectMapper) {
        JsonNode result;
        try {
            ArrayNode fieldNodes = objectMapper.createArrayNode();
            for (F field : fields) {
                fieldNodes.add(objectMapper.createObjectNode().put("name", field.getName()));
                fieldNodes.add(objectMapper.createObjectNode().put("label", field.getLabel()));
                fieldNodes.add(objectMapper.createObjectNode().put("data-type", field.getDataType().name()));
                fieldNodes.add(objectMapper.createObjectNode().put("required", field.isRequired()));
                fieldNodes.add(objectMapper.createObjectNode().put("id", field.getId()));
            }
            result = objectMapper.createObjectNode().set("fields", fieldNodes);
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set to JSON", e);
        }
        return result;
    }

}
