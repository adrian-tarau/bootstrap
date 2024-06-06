package net.microfalx.bootstrap.model;

import net.microfalx.lang.ClassUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A comparator for models.
 *
 * @param <M> the model type
 */
@Component
public class ModelComparator<M, F extends Field<M>, ID> implements Comparator<M> {

    private final MetadataService metadataService;
    private Metadata<M, F, ID> metadata;

    private final Set<F> excludedFields = new HashSet<>();
    private final Set<F> differenceFields = new LinkedHashSet<>();

    private boolean trackChanges = true;

    public ModelComparator(MetadataService metadataService) {
        requireNonNull(metadataService);
        this.metadataService = metadataService;
    }

    public boolean isTrackChanges() {
        return trackChanges;
    }

    public ModelComparator<M, F, ID> setTrackChanges(boolean trackChanges) {
        this.trackChanges = trackChanges;
        return this;
    }

    public ModelComparator<M, F, ID> exclude(F field) {
        requireNonNull(field);
        excludedFields.add(field);
        return this;
    }

    public ModelComparator<M, F, ID> exclude(String name) {
        return exclude(metadata.get(name));
    }

    public Set<F> getDifferencesFields() {
        return differenceFields;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public int compare(M o1, M o2) {
        if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }
        if (metadata == null) metadata = metadataService.getMetadata((Class<M>) o1.getClass());
        int result = 0;
        for (F field : metadata.getFields()) {
            Object value1 = field.get(o1);
            Object value2 = field.get(o2);
            if (value1 == null) {
                differenceFields.add(field);
                if (result == 0) result = -1;
            } else if (value2 == null) {
                differenceFields.add(field);
                if (result == 0) result = 1;
            } else if (value1 instanceof Comparable<?> && value1.getClass() == value2.getClass()) {
                int newResult = ((Comparable) value1).compareTo(value2);
                if (result == 0) result = newResult;
            } else if (ClassUtils.isJdkClass(value1)) {
                return value1.toString().compareTo(value2.toString());
            } else {
                ModelComparator<Object, Field<Object>, Object> newComparator = new ModelComparator<>(metadataService);
                int newResult = newComparator.compare(value1, value2);
                if (result == 0) result = newResult;
            }
            if (result != 0 && !trackChanges) break;
        }
        return result;
    }
}
