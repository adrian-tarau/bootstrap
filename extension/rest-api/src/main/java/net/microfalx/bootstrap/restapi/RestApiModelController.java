package net.microfalx.bootstrap.restapi;

import net.microfalx.bootstrap.dataset.State;
import net.microfalx.lang.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all REST API controllers which manage a model.
 *
 * @param <DTO>    the data transfer object type
 * @param <ENTITY> the entity type
 * @param <ID>     the record  identifier type
 */
public abstract class RestApiModelController<ENTITY, DTO, ID> extends RestApiController {

    @Autowired private ApplicationContext applicationContext;

    private RestApiMapper<ENTITY, DTO> mapper;

    /**
     * Subclasses must return the mapper class.
     *
     * @return the mapper class
     */
    protected abstract Class<? extends RestApiMapper<ENTITY, DTO>> getMapperClass();

    /**
     * Maps an entity to a DTO
     *
     * @param entity the entity
     * @return a non-null DTO
     */
    protected final DTO toDto(ENTITY entity) {
        requireNonNull(entity);
        DTO dto = getMapper().toDto(entity);
        if (dto == null) {
            throw new IllegalStateException("Mapper returned null DTO for entity: " + entity);
        }
        return dto;
    }

    /**
     * Maps a DTO to an entity
     *
     * @param dto the DTO
     * @return a non-null entity
     */
    protected final ENTITY toEntity(DTO dto) {
        requireNonNull(dto);
        ENTITY entity = getMapper().toEntity(dto);
        if (entity == null) {
            throw new IllegalStateException("Mapper returned null entity for DTO: " + dto);
        }
        return entity;
    }

    /**
     * Returns the mapper between DTOs and Entities
     *
     * @return a non-null instance
     */
    protected final RestApiMapper<ENTITY, DTO> getMapper() {
        if (mapper == null) {
            mapper = ClassUtils.create(getMapperClass());
            if (mapper instanceof AbstractRestApiMapper<?, ?> abstractRestApiMapper) {
                abstractRestApiMapper.configureMapper(abstractRestApiMapper.modelMapper);
                abstractRestApiMapper.setApplicationContext(applicationContext);
            }
        }
        return mapper;
    }

    /**
     * Validates the model after the default validation.
     *
     * @param dto    the DTO to validate
     * @param state  the current state of the data set
     * @param errors the errors to be reported to the client, for each field
     */
    protected void validate(DTO dto, State state, Map<String, String> errors) {
        // empty by default
    }
}
