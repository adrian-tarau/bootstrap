package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * Base class for all exporters.
 */
public abstract class DataSetExport<M, F extends Field<M>, ID> {

    private final Format format;

    private DataSet<M, F, ID> dataSet;

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
        Page<M> page = dataSet.findAll(Pageable.ofSize(5000));
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
        return dataSet.getDisplayValue(model, field);
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
        return dataSet.isExportable(field);
    }

    /**
     * Returns a list of fields which are exportable.
     *
     * @return a non-null instance
     * @see #isExportable(Field)
     * @see DataSet#isExportable(Field)
     */
    protected final List<F> getExportableFields() {
        return dataSet.getExportableFields().stream().filter(this::isExportable).toList();
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
        Resource resource = doExport(dataSet, page);
        return resource.withMimeType(getMimeType()).withName(getFileName(dataSet));
    }

    private MimeType getMimeType() {
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
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private String getFileName(DataSet<M, F, ID> dataSet) {
        return toIdentifier(dataSet.getName()) + "." + getFileExtension();
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
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    /**
     * An enum for the export format.
     */
    public enum Format {
        CSV, JSON, XML,TEXT,HTML
    }
}
