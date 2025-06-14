package net.microfalx.bootstrap.dsv;

import net.microfalx.bootstrap.model.AbstractMetadata;

import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A metadata class for DSV (Delimited Separated Values).
 */
public class DsvMetadata extends AbstractMetadata<DsvRecord, DsvField, String> {

    public DsvMetadata(String... columns) {
        super(DsvRecord.class);
        requireNonNull(columns);
        for (String column : columns) {
            addField(new DsvField(this, column, null));
        }
    }

    public DsvMetadata(List<String> columns) {
        super(DsvRecord.class);
        requireNonNull(columns);
        for (String column : columns) {
            addField(new DsvField(this, column, null));
        }
    }


}
