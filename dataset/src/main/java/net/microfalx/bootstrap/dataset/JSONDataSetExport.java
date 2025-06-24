package net.microfalx.bootstrap.dataset;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.springframework.data.domain.Page;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
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
            ObjectNode rootObject = null;
            ArrayNode rootArray = null;
            if (shouldExportOnlyData()) {
                rootArray = objectMapper.createArrayNode();
            } else {
                rootObject = objectMapper.createObjectNode();
            }
            extractMetadata(fields, rootObject);
            extractData(models, fields, rootObject, rootArray);

            DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
            printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            printer.indentObjectsWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            ObjectWriter writer = objectMapper.writer(printer);
            if (rootObject != null) {
                writer.writeValue(resource.getWriter(), rootObject);
            } else {
                writer.writeValue(resource.getWriter(), rootArray);
            }
            return resource;
        } catch (Exception e) {
            throw new DataSetExportException("Failed to export data set '" + dataSet.getName() + "' to JSON", e);
        }
    }

    private void extractMetadata(List<F> fields, ObjectNode root) {
        if (shouldExportOnlySchema()) {
            extractMetadataSchema(fields, root);
        } else if (isIncludeMetadata()) {
            extractMetadataInternal(fields, root);
        }
    }

    private void extractMetadataSchema(List<F> fields, ObjectNode root) {
        root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        root.put("title", getDataSet().getName());
        root.put("type", "object");
        ArrayNode requiredNode = root.putArray("required");
        fields.stream().filter(Field::isRequired).map(Field::getName).forEach(requiredNode::add);
        ObjectNode propertiesNode = root.withObjectProperty("properties");
        for (F field : fields) {
            ObjectNode node = propertiesNode.withObjectProperty(getName(field));
            extractMetadataSchemaField(field, node);
        }
    }

    private void extractMetadataInternal(List<F> fields, ObjectNode root) {
        ArrayNode fieldNodes = root.withArrayProperty("fields");
        for (F field : fields) {
            ObjectNode node = fieldNodes.addObject();
            extractMetadataInternalField(field, node);
        }
    }

    @SuppressWarnings("unchecked")
    private void extractMetadataSchemaField(F field, ObjectNode node) {
        Field.DataType dataType = field.getDataType();
        node.put("type", getDataTypeForSchema(dataType));
        String format = getFormatForSchema(dataType);
        if (format != null) node.put("format", format);
        if (dataType == Field.DataType.ENUM) {
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) field.getDataClass();
            ArrayNode enumNode = node.putArray("enum");
            for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
                enumNode.add(enumConstant.name());
            }
        }
        if (field.getDescription() != null) node.put("description", field.getDescription());
    }

    private void extractMetadataInternalField(F field, ObjectNode node) {
        node.put("name", getName(field));
        node.put("label", getLabel(field));
        node.put("required", field.isRequired());
        node.put("data-type", field.getDataType().name());
        node.put("id", field.isId());
        if (field.getDescription() != null) node.put("description", field.getDescription());
    }

    private void extractData(List<M> models, List<F> fields, ObjectNode rootObject, ArrayNode rootArray) {
        if (shouldExportOnlyData()) {
            extractDataExternal(rootArray, models, fields);
        } else if (isIncludeData()) {
            extractDataInternal(rootObject, models, fields);
        }
    }

    private void extractDataExternal(ArrayNode root, List<M> models, List<F> fields) {
        for (M model : models) {
            ObjectNode node = root.addObject();
            for (F field : fields) {
                Object value = getValue(model, field);
                if (value == null) continue;
                writeValueExternal(node, field, value);
            }
        }
    }

    private void extractDataInternal(ObjectNode root, List<M> models, List<F> fields) {
        ArrayNode dataArray = root.withArrayProperty("data");
        for (M model : models) {
            ArrayNode modelArray = dataArray.addArray();
            for (F field : fields) {
                Object value = getValue(model, field);
                writeValueInternal(modelArray, field, value);
            }
        }
    }

    private String getFormatForSchema(Field.DataType dataType) {
        return switch (dataType) {
            case DATE -> "date";
            case TIME -> "time";
            case DATE_TIME -> "date-time";
            default -> null;
        };
    }

    private String getDataTypeForSchema(Field.DataType dataType) {
        return switch (dataType) {
            case INTEGER -> "integer";
            case BOOLEAN -> "boolean";
            case NUMBER -> "number";
            default -> "string";
        };
    }

    private void writeValueExternal(ObjectNode node, F field, Object value) {
        String name = field.getName();
        if (value instanceof String) {
            node.put(name, (String) value);
        } else if (value instanceof Number) {
            node.put(name, ((Number) value).longValue());
        } else if (value instanceof Temporal) {
            node.put(name, DateTimeFormatter.ISO_DATE_TIME.format((Temporal) value));
        } else {
            node.put(name, value.toString());
        }
    }

    private void writeValueInternal(ArrayNode modelArray, F field, Object value) {
        if (value == null) {
            modelArray.addNull();
        } else if (value instanceof String) {
            modelArray.add((String) value);
        } else if (value instanceof Number) {
            modelArray.add(((Number) value).longValue());
        } else if (value instanceof Temporal) {
            modelArray.add(DateTimeFormatter.ISO_DATE_TIME.format((Temporal) value));
        } else {
            modelArray.add(value.toString());
        }
    }


}
