package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.ObjectUtils.defaultIfNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;

public class XMLDataSetExport<M, F extends Field<M>, ID> extends DataSetExport<M, F, ID> {

    protected XMLDataSetExport(Format format) {
        super(format);
    }

    @Override
    protected Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> page) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("dataset");
        List<F> fields = getExportableFields();
        exportMetadata(root, fields);
        exportData(root, fields, page.orElse(Page.empty()).getContent());
        Resource resource = TemporaryFileResource.file("temp", "xml");
        try {
            XMLWriter writer = new XMLWriter(resource.getWriter(), OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            throw new DataSetExportException("Failed to export data set '" + dataSet.getName() + "' to XML", e);
        }
        return resource;
    }

    private void exportMetadata(Element root, List<F> fields) {
        Element fieldsElement = root.addElement("fields");
        for (F field : fields) {
            Element fieldElement = fieldsElement.addElement("field");
            fieldElement.addAttribute("name", getName(field));
            fieldElement.addAttribute("label", getLabel(field));
            fieldElement.addAttribute("data-type", field.getDataType().name());
            fieldElement.addAttribute("required", Boolean.toString(field.isRequired()));
            fieldElement.addAttribute("id", Boolean.toString(field.isId()));
        }
    }

    private void exportData(Element root, List<F> fields, List<M> models) {
        Element dataElement = root.addElement("data");
        for (M model : models) {
            Element modelElement = dataElement.addElement("model");
            for (F field : fields) {
                String value = defaultIfNull(getValueAsString(model, field), EMPTY_STRING);
                modelElement.addElement("value").addText(value);
            }
        }
    }
}
