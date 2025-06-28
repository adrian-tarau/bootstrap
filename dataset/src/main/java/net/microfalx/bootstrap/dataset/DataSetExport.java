package net.microfalx.bootstrap.dataset;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.IOUtils;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import net.microfalx.resource.TemporaryFileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.rethrowExceptionAndReturn;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Base class for all exporters.
 *
 * When data and metadata are exported together, the format is typically exposing fast access to data using indexes,
 * and the position of the fields gives the order of the fields in the export.
 *
 * When schema is exported separately, the format is typically under an available standard for the export type
 * (like JSON schema, XML schema, etc.) and the order of the fields is not important.
 */
@Getter
public abstract class DataSetExport<M, F extends Field<M>, ID> implements Cloneable {

    /**
     * The default page size for the export.
     */
    public static final int DEFAULT_PAGE_SIZE = 500;

    /**
     * The format of the export.
     */
    private final Format format;

    /**
     * Whether to export the metadata (schema) of the data set.
     */
    @Setter private boolean includeMetadata = true;

    /**
     * Whether to export the data (without metadata/schema).
     */
    @Setter private boolean includeData = true;

    /**
     * Whether to export the metadata & metadata as separated files, under a zip archive.
     */
    @Setter private boolean multipleFiles = false;

    /**
     * Whether to export all the fields.
     */
    @Setter private boolean includeAll;

    @Getter(AccessLevel.PROTECTED) private DataSet<M, F, ID> dataSet;

    private List<F> exportedFields;
    private String[] fieldNames;
    private int[] fieldNameIndexes;
    private String[] fieldLabels;
    private int[] fieldLabelIndexes;
    private DataSetExportCallback<M, F, ID>[] callbacks = new DataSetExportCallback[0];

    /**
     * Creates an exporter for a given format.
     *
     * @param format the data format
     * @param <M>    the type of the model
     * @param <F>    the type of the field
     * @param <ID>   the type of the model identifier
     * @return a non-null instance
     */
    public static <M, F extends Field<M>, ID> DataSetExport<M, F, ID> create(final Format format) {
        requireNonNull(format);
        switch (format) {
            case CSV -> {
                return new CSVDataSetExport<>(format);
            }
            case XML -> {
                return new XMLDataSetExport<>(format);
            }
            case JSON -> {
                return new JSONDataSetExport<>(format);
            }
            case TEXT -> {
                return new TextDataSetExport<>(format);
            }
            case HTML -> {
                return new HTMLDataSetExport<>(format);
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    protected DataSetExport(Format format) {
        requireNonNull(format);
        this.format = format;
    }

    /**
     * Returns the export format.
     *
     * @return a non-null instance
     */
    public final Format getFormat() {
        return format;
    }

    /**
     * Exports the whole data set.
     *
     * @param dataSet the data set
     * @return a non-null resource
     * @see #export(DataSet, Page)
     */
    public final Resource export(DataSet<M, F, ID> dataSet) {
        requireNonNull(dataSet);
        Page<M> page = dataSet.findAll(Pageable.ofSize(DEFAULT_PAGE_SIZE));
        return doExportAndName(dataSet, Optional.of(page));
    }

    /**
     * Exports a page from the data set.
     *
     * @param dataSet the data set
     * @param page    the page to export
     * @return a non-null resource
     * @see #export(DataSet, Page)
     */
    public final Resource export(DataSet<M, F, ID> dataSet, Page<M> page) {
        requireNonNull(dataSet);
        requireNonNull(page);
        return doExportAndName(dataSet, Optional.of(page));
    }

    /**
     * Subclasses would implement the export
     *
     * @param dataSet the data set
     * @param page    the page, optional; if missing, the whole data set is exported
     * @return the resource holding the exported data
     */
    protected abstract Resource doExport(DataSet<M, F, ID> dataSet, Optional<Page<M>> page);

    /**
     * Returns the value to export for a given model and field.
     *
     * @param model the model
     * @param field the field
     * @return the value
     */
    protected final Object getValue(M model, F field) {
        requireNonNull(model);
        requireNonNull(field);
        Object value;
        if (field.isId() && field.getDataType().isNumeric()) {
            value = field.get(model);
        } else {
            value = getDisplayValue(model, field);
        }
        for (DataSetExportCallback<M, F, ID> callback : callbacks) {
            value = callback.getValue(dataSet, field, model, value);
        }
        return adaptValue(model, field, value);
    }

    /**
     * Returns the display value for a given model and field.
     *
     * @param model the model
     * @param field the field
     * @return the display value
     */
    protected Object getDisplayValue(M model, F field) {
        return dataSet.getDisplayValue(model, field);
    }

    /**
     * Adds a value to export for a given model and field.
     *
     * @param model the model
     * @param field the field
     * @param value the value
     * @return the adapted value to export
     */
    protected Object adaptValue(M model, F field, Object value) {
        return value;
    }

    /**
     * Returns the value to export for a given model and field as a string.
     *
     * @param model the model
     * @param field the field
     * @return the value
     */
    protected final String getValueAsString(M model, F field) {
        return Field.from(getValue(model, field), String.class);
    }

    /**
     * Returns whether the field should be exportable.
     *
     * @param field the field
     * @return {@code true} if exportable, {@code false}
     */
    protected final boolean isExportable(F field) {
        boolean exportable = dataSet.isExportable(field);
        for (DataSetExportCallback<M, F, ID> callback : callbacks) {
            exportable = callback.isExportable(dataSet, field, exportable);
        }
        return exportable;
    }

    /**
     * Returns a list of fields which are exportable.
     *
     * @return a non-null instance
     * @see #isExportable(Field)
     * @see DataSet#isExportable(Field)
     */
    protected final List<F> getExportableFields() {
        if (exportedFields == null) {
            List<F> fields;
            if (includeAll) {
                fields = dataSet.getFields();
            } else {
                fields = dataSet.getFields().stream().filter(this::isExportable).toList();
            }
            exportedFields = fields.stream().filter(this::isExportable).collect(Collectors.toList());
        }
        return exportedFields;
    }

    /**
     * Return whether the export should include schema (metadata) only.
     *
     * @return {@code true} if only schema should be exported, {@code false} otherwise
     */
    protected final boolean shouldExportOnlySchema() {
        return includeMetadata && !includeData;
    }

    /**
     * Return whether the export should include data only.
     *
     * @return {@code true} if only data should be exported, {@code false} otherwise
     */
    protected final boolean shouldExportOnlyData() {
        return !includeMetadata && includeData;
    }

    /**
     * Returns the exported field name for a given field.
     */
    protected final String getName(F field) {
        requireNonNull(field);
        if (fieldNames == null) {
            List<F> fields = getExportableFields();
            fieldNames = new String[fields.size()];
            fieldNameIndexes = new int[getDataSet().getMetadata().getFields().size()];
            int index = 0;
            for (F exportableField : fields) {
                fieldNames[index] = extractFieldName(exportableField);
                fieldNameIndexes[exportableField.getIndex()] = index;
                index++;
            }
        }
        int index = fieldNameIndexes[field.getIndex()];
        return fieldNames[index];
    }

    /**
     * Returns the exported field label for a given field.
     */
    protected final String getLabel(F field) {
        requireNonNull(field);
        if (fieldLabels == null) {
            List<F> fields = getExportableFields();
            fieldLabels = new String[fields.size()];
            fieldLabelIndexes = new int[getDataSet().getMetadata().getFields().size()];
            int index = 0;
            for (F exportableField : fields) {
                fieldLabels[index] = extractFieldName(exportableField);
                fieldLabelIndexes[exportableField.getIndex()] = index;
                index++;
            }
        }
        int index = fieldLabelIndexes[field.getIndex()];
        return fieldLabels[index];
    }

    /**
     * Subclasses would implement the export
     *
     * @param dataSet the data set
     * @param page    the page, optional; if missing, the whole data set is exported
     * @return the resource holding the exported data
     */
    private Resource doExportAndName(DataSet<M, F, ID> dataSet, Optional<Page<M>> page) {
        this.dataSet = dataSet;
        initCallbacks();
        Resource resource;
        if (multipleFiles) {
            resource = exportMultipleFiles(dataSet, page.orElse(Page.empty()));
        } else {
            resource = doExport(dataSet, page);
        }
        return resource.withMimeType(getMimeType()).withName(getFileName(dataSet));
    }

    private MimeType getMimeType() {
        if (multipleFiles) {
            return MimeType.APPLICATION_ZIP;
        } else {
            switch (format) {
                case CSV -> {
                    return MimeType.TEXT_CSV;
                }
                case XML -> {
                    return MimeType.TEXT_XML;
                }
                case JSON -> {
                    return MimeType.APPLICATION_JSON;
                }
                case TEXT -> {
                    return MimeType.TEXT;
                }
                case HTML -> {
                    return MimeType.TEXT_HTML;
                }
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            }
        }
    }

    private String getFileName(DataSet<M, F, ID> dataSet) {
        String fileName = toIdentifier(dataSet.getName());
        if (shouldExportOnlySchema()) {
            fileName += "-schema";
        } else if (shouldExportOnlyData()) {
            fileName += "-data";
        }
        String extension;
        if (multipleFiles) {
            extension = "zip";
        } else {
            extension = getFileExtension();
        }
        return fileName + "." + extension;
    }

    private Resource exportMultipleFiles(DataSet<M, F, ID> dataSet, Page<M> page) {
        try {
            Resource zipResource = TemporaryFileResource.file("temp", "zip");
            try (ZipOutputStream zipStream = new ZipOutputStream(zipResource.getOutputStream())) {
                exportToZip(dataSet, page, true, zipStream);
                exportToZip(dataSet, page, false, zipStream);
            }
            return zipResource;
        } catch (IOException e) {
            throw new DataSetExportException("Failed to export metadata & data for data set '" + dataSet.getName() + "' to a ZIP", e);
        }
    }

    private void exportToZip(DataSet<M, F, ID> dataSet, Page<M> page, boolean metadata, ZipOutputStream zipStream) throws IOException {
        DataSetExport<M, F, ID> metadataExport = copy().setIncludeMetadata(metadata).setIncludeData(!metadata).setMultipleFiles(false);
        String fileName = metadataExport.getFileName(dataSet);
        Resource resource = metadataExport.export(dataSet, page);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipEntry.setCreationTime(FileTime.fromMillis(currentTimeMillis()));
        zipEntry.setLastModifiedTime(zipEntry.getCreationTime());
        zipEntry.setSize(resource.length());
        zipStream.putNextEntry(zipEntry);
        IOUtils.appendStream(zipStream, resource.getInputStream(), false);
        zipStream.closeEntry();
    }

    private String getFileExtension() {
        switch (format) {
            case CSV -> {
                return "csv";
            }
            case XML -> {
                return "xml";
            }
            case JSON -> {
                return "json";
            }
            case TEXT -> {
                return "text";
            }
            case HTML -> {
                return "html";
            }
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    @SuppressWarnings("unchecked")
    private DataSetExport<M, F, ID> copy() {
        try {
            return (DataSetExport<M, F, ID>) clone();
        } catch (CloneNotSupportedException e) {
            return rethrowExceptionAndReturn(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initCallbacks() {
        Collection<DataSetExportCallback> dataSetExportCallbacks = ClassUtils.resolveProviderInstances(DataSetExportCallback.class);
        callbacks = dataSetExportCallbacks.stream().filter(c -> c.supports(dataSet)).toList().toArray(new DataSetExportCallback[0]);
    }

    private String extractFieldName(F field) {
        requireNonNull(field);
        for (DataSetExportCallback<M, F, ID> callback : callbacks) {
            String name = callback.getFieldName(dataSet, field);
            if (name != null) return name;
        }
        return field.getName();
    }

    private String extractLabel(F field) {
        requireNonNull(field);
        for (DataSetExportCallback<M, F, ID> callback : callbacks) {
            String label = callback.getLabel(dataSet, field);
            if (label != null) return label;
        }
        return field.getLabel();
    }

    /**
     * An enum for the export format.
     */
    public enum Format {
        CSV, JSON, XML,TEXT,HTML
    }
}
