package net.microfalx.bootstrap.dsv;

import net.microfalx.bootstrap.dataset.AbstractDataSet;
import net.microfalx.bootstrap.dataset.AbstractDataSetFactory;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.DataSetFactory;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.ModelFilter;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.resource.Resource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * An implementation of a data set for Delimiter-Separated Values (DSV) files.
 */
public class DsvDataSet extends AbstractDataSet<DsvRecord, DsvField, String> {

    private Resource resource;
    private DsvOptions options;

    /**
     * Creates data set for the specified resource and options.
     * <p>
     * The DSV content is expected to be in a standard CSV format.
     *
     * @param resource the resource representing the DSV content
     * @return a non-null instance
     * @throws IOException if an error occurs while reading the resource
     */
    public static DsvDataSet create(Resource resource) throws IOException {
        return create(resource, DsvOptions.builder().build());
    }

    /**
     * Creates data set for the specified resource and options.
     *
     * @param resource the resource representing the DSV content
     * @param options  the options for parsing the DSV content
     * @return a non-null instance
     * @throws IOException if an error occurs while reading the resource
     */
    public static DsvDataSet create(Resource resource, DsvOptions options) throws IOException {
        requireNonNull(resource);
        requireNonNull(options);
        DsvMetadata metadata = createMetadata(resource, options);
        Factory factory = new Factory();
        DsvDataSet dataSet = (DsvDataSet) factory.create(metadata);
        dataSet.resource = resource;
        dataSet.options = options;
        return dataSet;
    }

    public DsvDataSet(DataSetFactory<DsvRecord, DsvField, String> factory, Metadata<DsvRecord, DsvField, String> metadata) {
        super(factory, metadata);
    }

    @Override
    protected List<DsvRecord> doFindAll() {
        return doFindAll(Pageable.unpaged(), Filter.EMPTY).toList();
    }

    @Override
    protected Page<DsvRecord> doFindAll(Pageable pageable, Filter filterable) {
        CSVFormat format = createCsvFormat(options);
        DsvMetadata metadata = (DsvMetadata) getMetadata();
        List<DsvRecord> records = new ArrayList<>();
        try (Reader reader = resource.getReader()) {
            CSVParser parser = format.parse(reader);
            ModelFilter<DsvRecord> filter = new ModelFilter<>(getMetadata(), new CsvRecordIterable(parser, metadata), filterable);
            filter.toStream().forEach(records::add);
        } catch (IOException e) {
            throw new DataSetException("Failed to read DSV records from " + resource, e);
        }
        return getPage(records, pageable);
    }


    public static DsvMetadata createMetadata(Resource resource, DsvOptions options) throws IOException {
        requireNonNull(resource);
        requireNonNull(options);
        CSVFormat format = createCsvFormat(options);
        try (Reader reader = resource.getReader();
             CSVParser parser = format.parse(reader)) {
            return new DsvMetadata(parser.getHeaderNames());
        }
    }

    private static CSVFormat createCsvFormat(DsvOptions options) {
        CSVFormat.Builder builder = CSVFormat.RFC4180.builder();
        builder.setDelimiter(options.getDelimiter())
                .setQuote(options.getQuote()).setSkipHeaderRecord(options.isHeader());
        if (!ObjectUtils.isEmpty(options.getColumns())) {
            builder.setHeader(options.getColumns());
        } else {
            builder.setHeader();
        }
        return builder.get();
    }

    public static class Factory extends AbstractDataSetFactory<DsvRecord, DsvField, String> {

        @Override
        protected AbstractDataSet<DsvRecord, DsvField, String> doCreate(Metadata<DsvRecord, DsvField, String> metadata) {
            return new DsvDataSet(this, metadata);
        }

        @Override
        public boolean supports(Metadata<DsvRecord, DsvField, String> metadata) {
            return metadata instanceof DsvMetadata;
        }
    }

    private static class CsvRecordIterable implements Iterable<DsvRecord> {

        private final CSVParser parser;
        private final DsvMetadata metadata;

        public CsvRecordIterable(CSVParser parser, DsvMetadata metadata) {
            this.parser = parser;
            this.metadata = metadata;
        }

        @Override
        public java.util.Iterator<DsvRecord> iterator() {
            return new java.util.Iterator<>() {
                private final java.util.Iterator<org.apache.commons.csv.CSVRecord> csvIterator = parser.iterator();

                @Override
                public boolean hasNext() {
                    return csvIterator.hasNext();
                }

                @Override
                public DsvRecord next() {
                    return new DsvRecord(metadata, csvIterator.next());
                }
            };
        }
    }
}
