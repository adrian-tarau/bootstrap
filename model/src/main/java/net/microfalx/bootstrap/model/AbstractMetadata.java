package net.microfalx.bootstrap.model;

import net.microfalx.lang.*;
import net.microfalx.lang.annotation.Glue;
import net.microfalx.lang.annotation.I18n;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.lang.annotation.Annotation;
import java.util.*;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.ClassUtils.isJdkClass;
import static net.microfalx.lang.ObjectUtils.isNull;
import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * Base class for all metadata.
 *
 * @param <M> the model type
 */
public abstract class AbstractMetadata<M, F extends Field<M>, ID> implements Metadata<M, F, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);

    private final String id;
    private String name;
    private String description;
    private final Class<M> modelClass;
    private final List<F> fields = new ArrayList<>();
    private final Map<String, F> fieldsById = new HashMap<>();
    private F idField;
    private final List<F> idFields = new ArrayList<>();
    private final Map<String, F> idFieldsById = new HashMap<>();
    private F timestampField;
    private Class<ID> idClass;

    MetadataService metadataService;
    MessageSource messageSource;

    public AbstractMetadata(Class<M> modelClass) {
        this.modelClass = modelClass;
        this.id = modelClass.getName();
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    protected final void setName(String name) {
        requireNotEmpty(name);
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    protected final void setDescription(String description) {
        this.description = description;
    }

    @Override
    public final Class<M> getModel() {
        return modelClass;
    }

    @Override
    public M create() {
        try {
            return modelClass.newInstance();
        } catch (Exception e) {
            return ExceptionUtils.throwException(e);
        }
    }

    @Override
    public final List<F> getFields() {
        return unmodifiableList(fields);
    }

    @Override
    public final List<F> getFields(Field.DataType dataType) {
        requireNonNull(dataType);
        return getFields().stream().filter(field -> field.getDataType() == dataType).toList();
    }


    @Override
    public List<F> getIdFields() {
        return unmodifiableList(idFields);
    }

    @Override
    public List<F> getNameFields() {
        List<F> nameFields = fields.stream().filter(f -> f.hasAnnotation(Name.class)).toList();
        if (nameFields.isEmpty()) {
            Optional<F> firstString = fields.stream().filter(f -> f.getDataType() == Field.DataType.STRING).findFirst();
            if (firstString.isEmpty()) {
                throw new FieldNotFoundException("At least one field must be annotated with @Name " +
                        "or at least one String field for model " + ClassUtils.getName(getModel()));
            }
            nameFields = Arrays.asList(firstString.get());
        }

        return Collections.unmodifiableList(nameFields);
    }

    @Override
    public F findIdField() {
        if (idFields.size() > 1) throw new ModelException("Multiple identifier fields are present for " + getName());
        return idField != null ? idField : null;
    }

    @Override
    public F findTimestampField() {
        return timestampField;
    }

    @Override
    public F find(String nameOrProperty) {
        requireNonNull(nameOrProperty);
        return fieldsById.get(StringUtils.toIdentifier(nameOrProperty));
    }

    @SuppressWarnings("unchecked")
    @Override
    public F get(String nameOrProperty) {
        Field<?> field = find(nameOrProperty);
        if (field == null) {
            throw new FieldNotFoundException("A field with name or property '" + nameOrProperty + "' is not registered in " + getName());
        }
        return (F) field;
    }

    @Override
    public String getName(M model) {
        requireNonNull(model);
        StringBuilder builder = new StringBuilder();
        List<F> nameFields = getNameFields();
        for (F nameField : nameFields) {
            Glue glueAnnot = nameField.findAnnotation(Glue.class);
            String separator = glueAnnot != null ? glueAnnot.value() : " ";
            if (builder.length() > 0) builder.append(separator);
            if (glueAnnot != null && StringUtils.isNotEmpty(glueAnnot.before())) builder.append(glueAnnot.before());
            builder.append(nameField.get(model));
            if (glueAnnot != null && StringUtils.isNotEmpty(glueAnnot.after())) builder.append(glueAnnot.after());
        }
        return builder.toString();
    }

    @Override
    public Class<ID> getIdClass() {
        return idClass;
    }

    @Override
    public CompositeIdentifier<M, F, ID> getId(M model) {
        return new CompositeIdentifier<>(this, model);
    }

    @Override
    public CompositeIdentifier<M, F, ID> getId(String id) {
        return new CompositeIdentifier<>(this, id);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean identical(M firstModel, M secondModel) {
        if (firstModel == null && secondModel == null) return true;
        if (isNull(firstModel, secondModel)) return false;
        for (F field : fields) {
            Object firstValue = field.get(firstModel);
            Object secondValue = field.get(secondModel);
            if (firstValue == null && secondValue == null) continue;
            if (isNull(firstValue, secondValue)) return false;
            Class<?> type = firstValue.getClass();
            if (isJdkClass(type)) {
                if (!ObjectUtils.equals(firstValue, secondValue)) return false;
            } else {
                Metadata metadata = metadataService.getMetadata(type);
                if (!metadata.identical(firstValue, secondValue)) return false;
            }
        }
        return true;
    }

    @Override
    public M copy(M model) {
        return copy(model, false);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public M copy(M model, boolean deep) {
        if (model == null) return null;
        M newModel = create();
        for (F field : fields) {
            Object value = field.get(model);
            if (value != null) {
                if (deep) {
                    if (ClassUtils.isJdkClass(value)) {
                        value = ObjectUtils.copy(value);
                    } else {
                        Metadata metadata = metadataService.getMetadata(value);
                        value = metadata.copy(value, true);
                    }
                }
                field.set(newModel, value);
            }
        }
        return newModel;
    }

    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationClass) {
        return modelClass.getAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass) {
        return findAnnotation(annotationClass) != null;
    }

    /**
     * Adds a new field to the metadata.
     *
     * @param field the field
     */
    public void addField(F field) {
        requireNonNull(field);
        fields.add(field);
        fieldsById.put(field.getId(), field);
        fieldsById.put(StringUtils.toIdentifier(field.getName()), field);
        fieldsById.put(StringUtils.toIdentifier(field.getProperty()), field);

        if (field.isId()) {
            idField = field;
            idFields.add(field);
            idFieldsById.put(field.getId(), field);
            idFieldsById.put(StringUtils.toIdentifier(field.getName()), field);
            idFieldsById.put(StringUtils.toIdentifier(field.getProperty()), field);
        }
        if (idFields.size() > 1) idField = null;
        if (field.hasAnnotation(Timestamp.class)) timestampField = field;
    }

    /**
     * Invoked after creation to initialize the metadata
     */
    protected void initialize() {
        initName();
        initI18n();
    }

    /**
     * Returns the I18n prefix for the metadata.
     *
     * @return a non-null instance
     */
    protected final String getI18nPrefix() {
        I18n i18nAnnot = AnnotationUtils.getAnnotation(getModel(), I18n.class);
        String i18nPrefix = "model." + (i18nAnnot != null ? i18nAnnot.value() : StringUtils.toIdentifier(getModel().getSimpleName()));
        i18nPrefix += ".";
        return i18nPrefix;
    }

    /**
     * Returns the value associated with a key
     *
     * @param key the key
     * @return the value, null if not defined
     */
    protected final String getI18n(String key) {
        try {
            return messageSource.getMessage(key, ObjectUtils.EMPTY_ARRAY, Locale.US);
        } catch (NoSuchMessageException e) {
            LOGGER.debug("Missing i18n '" + key + "' for model " + getModel().getName());
            return null;
        }
    }

    private void initI18n() {
        this.name = defaultIfEmpty(getI18n(getI18nPrefix() + "name"), this.name);
        this.description = getI18n(getI18nPrefix() + "description");
    }

    private void initName() {
        Name nameAnnot = findAnnotation(Name.class);
        if (nameAnnot != null) {
            this.name = nameAnnot.value();
        } else {
            this.name = StringUtils.beautifyCamelCase(modelClass.getSimpleName());
        }
    }


    @Override
    public String toString() {
        return "AbstractMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", modelClass=" + modelClass +
                ", fields=" + fields.size() +
                '}';
    }
}
