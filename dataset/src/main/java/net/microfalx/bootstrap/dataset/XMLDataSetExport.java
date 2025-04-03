package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
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

public class XMLDataSetExport<M, F extends Field<M>, ID> extends DataSetExport<M, F, ID> {

    protected XMLDataSetExport(Format format) {
        super(format);
    }

    @Override
    protected Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> page) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("dataset");
        Metadata<M, F, ID> metadata = dataSet.getMetadata();
        List<F> fields = metadata.getFields();
        exportMetadata(root, fields);
        exportData(root, fields, page.orElse(Page.empty()).getContent());
        Resource resource = TemporaryFileResource.file("temp", "xml");
        try {
            XMLWriter writer = new XMLWriter(resource.getWriter(), OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            throw new DataSetExportException("Failed to export data set to XML", e);
        }
        return resource;
    }

    private void exportMetadata(Element root, List<F> fields) {
        Element fieldsElement = root.addElement("fields");
        for (F field : fields) {
            Element fieldElement = fieldsElement.addElement("field");
            fieldElement.addAttribute("name", field.getName());
            fieldElement.addAttribute("label", field.getLabel());
            fieldElement.addAttribute("data-type", field.getDataType().name());
            fieldElement.addAttribute("required", Boolean.toString(field.isRequired()));
            fieldElement.addAttribute("id", field.getId());
        }
    }

    private void exportData(Element root, List<F> fields, List<M> models) {
        Element dataElement = root.addElement("data");
        for (M model : models) {
            Element modelElement = dataElement.addElement("model");
            for (F field : fields) {
                modelElement.addElement("value").addText(ObjectUtils.defaultIfNull(field.get(model, String.class), StringUtils.EMPTY_STRING));
            }
        }
    }
}
