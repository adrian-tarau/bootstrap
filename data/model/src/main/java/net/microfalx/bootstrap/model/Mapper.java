package net.microfalx.bootstrap.model;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ExceptionUtils;
import net.microfalx.lang.Logger;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A helper class to map a source object to a target object.
 *
 * @param <S> the source type
 * @param <D> the destination type
 */
@Slf4j
public final class Mapper<S, D> implements Cloneable {

    private final Class<S> sourceType;
    private final Class<D> destinationType;

    private Metadata<S, Field<S>, ?> sourceMetadata;
    private Metadata<D, Field<D>, ?> destinationMetadata;

    private boolean recursive;
    private BiConsumer<S, D> mapper;
    private FieldCallback<S, D> ignoredFieldsCallback;
    private final Set<Class<? extends Annotation>> ignoreAnnotations = new HashSet<>();
    private final Set<String> ignoreFields = new HashSet<>();
    private boolean ignoreComplexTypes;
    private final Map<String, String> mappedFields = new HashMap<>();
    private final Collection<FieldMapperImpl<S, D>> fieldMappers = new ArrayList<>();
    private final Collection<FieldMapperImpl<S, D>> ignoredFieldMappers = new ArrayList<>();

    private static volatile MetadataService metadataService;
    private static final Map<Class<?>, Map<Class<?>, Mapper<?, ?>>> registry = new ConcurrentHashMap<>();

    /**
     * Creates a Mapper builder for the given source and destination types.
     *
     * @param sourceType      the source type
     * @param destinationType the destination type
     * @param <S>             the source type
     * @param <D>             the destination type
     * @return a new Mapper builder
     */
    public static <S, D> Builder<S, D> builder(Class<S> sourceType, Class<D> destinationType) {
        return new Builder<>(sourceType, destinationType);
    }

    /**
     * Registers a Mapper in the registry.
     *
     * @param sourceType      the source type
     * @param destinationType the destination type
     * @param <S>             the source type
     * @param <D>             the destination type
     */
    public static <S, D> void register(Class<S> sourceType, Class<D> destinationType) {
        builder(sourceType, destinationType).register();
    }

    /**
     * Registers a Mapper in the registry.
     *
     * @param mapper the mapper to register
     * @param <S>    the source type
     * @param <D>    the destination type
     */
    public static <S, D> void register(Mapper<S, D> mapper) {
        requireNonNull(mapper);
        Map<Class<?>, Mapper<?, ?>> cache = registry.computeIfAbsent(mapper.getSourceMetadata().getModel(), k -> new ConcurrentHashMap<>());
        cache.put(mapper.getDestinationMetadata().getModel(), mapper);
    }

    /**
     * Returns a Mapper for the given source and destination types.
     *
     * @param source          the source object
     * @param destinationType the destination type
     * @param <S>             the source type
     * @param <D>             the destination type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public static <S, D> Mapper<S, D> get(S source, Class<D> destinationType) {
        requireNonNull(source);
        return get((Class<S>) source.getClass(), destinationType);
    }

    /**
     * Returns a Mapper for the given source and destination types.
     *
     * @param sourceType      the source type
     * @param destinationType the destination type
     * @param <S>             the source type
     * @param <D>             the destination type
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    public static <S, D> Mapper<S, D> get(Class<S> sourceType, Class<D> destinationType) {
        requireNonNull(sourceType);
        requireNonNull(destinationType);
        validateType(sourceType, "Source");
        validateType(destinationType, "Target");
        Mapper<S, D> mapper = null;
        Map<Class<?>, Mapper<?, ?>> mappersFor = registry.get(sourceType);
        if (mappersFor != null) {
            mapper = ((Mapper<S, D>) mappersFor.get(destinationType));
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper for type '" + ClassUtils.getName(sourceType) + "' to type '" + ClassUtils.getName(sourceType) + "' found");
        }
        return mapper;
    }

    private Mapper(Class<S> sourceType, Class<D> destinationType) {
        requireNonNull(sourceType);
        requireNonNull(destinationType);
        this.sourceType = sourceType;
        this.destinationType = destinationType;
    }

    /**
     * Returns the metadata for the source type.
     *
     * @return a non-null instance
     */
    public Metadata<S, Field<S>, ?> getSourceMetadata() {
        return sourceMetadata;
    }

    /**
     * Returns the metadata for the destination type.
     *
     * @return a non-null instance
     */
    public Metadata<D, Field<D>, ?> getDestinationMetadata() {
        return destinationMetadata;
    }

    /**
     * Returns whether the mapping should be applied recursively to nested objects.
     *
     * @return {@code true} if the mapping should be applied recursively, {@code false} otherwise
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Returns the custom mapping function, if defined.
     *
     * @return the mapper function, or null if not defined
     */
    public BiConsumer<S, D> getMapper() {
        return mapper;
    }

    /**
     * Returns whether to ignore complex types (i.e. JDK simple types) during mapping.
     *
     * @return {@code true} to ignore complex types, {@code false} otherwise
     */
    public boolean isIgnoreComplexTypes() {
        return ignoreComplexTypes;
    }

    /**
     * Returns the callback for ignored fields, if defined.
     *
     * @return the ignored fields callback, or null if not defined
     */
    public FieldCallback<S, D> getIgnoredFieldsCallback() {
        return ignoredFieldsCallback;
    }

    /**
     * Creates a new destination object and copies the values from the source object.
     *
     * @param source the source object
     * @return the new destination object
     */
    public D to(S source) {
        requireNonNull(source);
        D destination = destinationMetadata.create();
        copy(source, destination);
        return destination;
    }

    /**
     * Copies the values from the source object to the destination object.
     *
     * @param source      the source  object
     * @param destination the destination object
     */
    public void copy(S source, D destination) {
        requireNonNull(source);
        for (FieldMapperImpl<S, D> fieldMapper : fieldMappers) {
            fieldMapper.copy(source, destination);
        }
        if (ignoredFieldsCallback != null) {
            for (FieldMapperImpl<S, D> ignoredFieldMapper : ignoredFieldMappers) {
                ignoredFieldsCallback.doWithField(ignoredFieldMapper, source, destination);
            }
        }
        if (mapper != null) mapper.accept(source, destination);
    }

    @SuppressWarnings("unchecked")
    private Mapper<S, D> copy() {
        try {
            return (Mapper<S, D>) super.clone();
        } catch (CloneNotSupportedException e) {
            return ExceptionUtils.rethrowExceptionAndReturn(e);
        }
    }

    static void initialize(MetadataService metadataService) {
        Mapper.metadataService = metadataService;
    }

    private void initialize() {
        Logger logger = Logger.create();
        sourceMetadata = getMetadata(sourceType);
        destinationMetadata = getMetadata(destinationType);
        for (Field<S> sourceField : sourceMetadata.getFields()) {
            if (shouldIgnore(sourceField)) {
                logger.info(" - ''{0}'' ignored by name or annotation", sourceField.getName());
                ignoredFieldMappers.add(new FieldMapperImpl<>(this, sourceField, null));
            } else if (!ignoreComplexTypes || isBaseClass(sourceField)) {
                String destinationFieldName = mappedFields.get(toIdentifier(sourceField.getName()));
                if (destinationFieldName == null) destinationFieldName = sourceField.getName();
                Field<D> destinationField = destinationMetadata.find(destinationFieldName);
                if (destinationField != null) {
                    logger.info(" - ''{0}'' mapped", sourceField.getName());
                    fieldMappers.add(new FieldMapperImpl<>(this, sourceField, destinationField));
                } else {
                    logger.info(" - ''{0}'' ignored, no target field", sourceField.getName());
                }
            } else {
                logger.info(" - ''{0}'' ignored, complex type", sourceField.getName());
            }
        }
        LOGGER.debug("Registered mapping between {} -> {}, fields:\n{}", ClassUtils.getName(sourceType),
                ClassUtils.getName(destinationType), logger.getOutput());
    }

    private boolean isBaseClass(Field<S> sourceField) {
        Class<?> dataClass = sourceField.getDataClass();
        return ClassUtils.isBaseClass(dataClass) || dataClass.isEnum()
                || ClassUtils.isSubClassOf(dataClass, Collection.class)
                || ClassUtils.isSubClassOf(dataClass, Map.class);
    }

    private boolean shouldIgnore(Field<S> field) {
        if (ignoreFields.contains(toIdentifier(field.getName()))) {
            return true;
        }
        return ignoreAnnotations.stream().anyMatch(annotation -> field.findAnnotation(annotation) != null);
    }

    private static <M> Metadata<M, Field<M>, ?> getMetadata(Class<M> type) {
        if (metadataService == null) {
            throw new IllegalStateException("Metadata service has not been initialized");
        }
        return metadataService.getMetadata(type);
    }

    private static void validateType(Class<?> type, String name) {
        if (ClassUtils.isBaseClass(type)) {
            throw new IllegalArgumentException(name + " type must not be a JDK base class");
        }
    }

    public final static class Builder<S, D> {

        private final Class<S> sourceType;
        private final Class<D> destinationType;

        private BiConsumer<S, D> mapper;
        private FieldCallback<S, D> ignoredFieldsCallback;
        private final Set<Class<? extends Annotation>> ignoreAnnotations = new HashSet<>();
        private boolean ignoreComplexTypes;
        private final Set<String> ignoreFields = new HashSet<>();
        private final Map<String, String> mappedFields = new HashMap<>();

        private boolean recursive;

        private Builder(Class<S> sourceType, Class<D> destinationType) {
            requireNonNull(sourceType);
            requireNonNull(destinationType);
            validateType(sourceType, "Source");
            validateType(destinationType, "Target");
            this.sourceType = sourceType;
            this.destinationType = destinationType;
        }

        public Builder<S, D> recursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public Builder<S, D> mapper(BiConsumer<S, D> mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder<S, D> ignoredFieldsCallback(FieldCallback<S, D> ignoredFieldsCallback) {
            this.ignoredFieldsCallback = ignoredFieldsCallback;
            return this;
        }

        public Builder<S, D> ignoreAnnotation(Class<? extends Annotation> ignoreAnnotation) {
            requireNonNull(ignoreAnnotation);
            this.ignoreAnnotations.add(ignoreAnnotation);
            return this;
        }

        public Builder<S, D> ignoreComplexTypes(boolean ignoreComplexTypes) {
            this.ignoreComplexTypes = ignoreComplexTypes;
            return this;
        }

        @SafeVarargs
        public final Builder<S, D> ignoreAnnotations(Class<? extends Annotation>... ignoreAnnotations) {
            if (ignoreAnnotations != null) this.ignoreAnnotations.addAll(Arrays.asList(ignoreAnnotations));
            return this;
        }

        public Builder<S, D> ignoreField(String name) {
            requireNotEmpty(name);
            this.ignoreFields.add(toIdentifier(name));
            return this;
        }

        public Builder<S, D> mapField(String sourceName, String destinationName) {
            requireNotEmpty(sourceName);
            requireNotEmpty(destinationName);
            this.mappedFields.put(toIdentifier(sourceName), toIdentifier(destinationName));
            return this;
        }

        public void register() {
            Mapper.register(build());
        }

        public Mapper<S, D> build() {
            Mapper<S, D> mapper = new Mapper<>(sourceType, destinationType);
            mapper.recursive = this.recursive;
            mapper.mapper = this.mapper;
            mapper.ignoredFieldsCallback = this.ignoredFieldsCallback;
            mapper.ignoreComplexTypes = this.ignoreComplexTypes;
            mapper.ignoreAnnotations.addAll(this.ignoreAnnotations);
            mapper.ignoreFields.addAll(this.ignoreFields);
            mapper.mappedFields.putAll(this.mappedFields);
            mapper.initialize();
            return mapper;
        }
    }

    /**
     * A callback for a field
     *
     * @param <S> the source type
     * @param <D> the destination type
     */
    public interface FieldCallback<S, D> {

        /**
         * Invoked for a field that is being mapped.
         *
         * @param fieldMapper the field mapper for the field being mapped
         * @param source      the source record
         * @param destination the destination record
         */
        void doWithField(FieldMapper<S, D> fieldMapper, S source, D destination);

    }

    /**
     * A field mapper that defines how to copy a field from the source object to the destination object.
     *
     * @param <S> the source type
     * @param <D> the destination type
     */
    public interface FieldMapper<S, D> {

        /**
         * The Mapper that this field mapper belongs to.
         *
         * @return a non-null instance
         */
        Mapper<S, D> getMapper();

        /**
         * The source field.
         *
         * @return a non-null instance
         */
        Field<S> getSourceField();

        /**
         * Returns the destination field.
         *
         * @return a non-null instance, null if the destination field is not available
         */
        Field<D> getDestinationField();

    }

    private static final class FieldMapperImpl<S, D> implements FieldMapper<S, D> {

        private final Mapper<S, D> mapper;
        private final Field<S> sourceField;
        private final Field<D> destinationField;

        private FieldMapperImpl(Mapper<S, D> mapper, Field<S> sourceField, Field<D> destinationField) {
            this.mapper = mapper;
            this.sourceField = sourceField;
            this.destinationField = destinationField;
        }

        @Override
        public Mapper<S, D> getMapper() {
            return mapper;
        }

        @Override
        public Field<S> getSourceField() {
            return sourceField;
        }

        @Override
        public Field<D> getDestinationField() {
            return destinationField;
        }

        private void copy(S source, D destination) {
            try {
                Object value = sourceField.get(source);
                destinationField.set(destination, value);
            } catch (Exception e) {
                throw new ModelException("Failed to copy field '" + sourceField.getName() + "' from '" + ClassUtils.getName(source) + "' to '" + ClassUtils.getName(destination) + "'", e);
            }
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", FieldMapperImpl.class.getSimpleName() + "[", "]").add("sourceField=" + sourceField.getName()).add("destinationField=" + destinationField.getName()).toString();
        }
    }
}
