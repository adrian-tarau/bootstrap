package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.resource.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all exporters.
 */
public abstract class DataSetExport<M, F extends Field<M>, ID> {

    private final Format format;

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
        return doExport(dataSet, Optional.of(page));
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
        return doExport(dataSet, Optional.of(page));
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
     * An enum for the export format.
     */
    public enum Format {
        CSV, JSON, XML
    }
}
