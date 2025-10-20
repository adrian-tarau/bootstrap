package net.microfalx.bootstrap.restapi;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.lang.ClassUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for all REST API mappers.
 *
 * @param <ENTITY> the entity type
 * @param <DTO>    the data transfer object type
 */
public abstract class AbstractRestApiMapper<ENTITY, DTO> extends ApplicationContextSupport
        implements RestApiMapper<ENTITY, DTO>, InitializingBean {

    final ModelMapper modelMapper = new ModelMapper();

    @Override
    public final DTO toDto(ENTITY entity) {
        if (entity == null) return null;
        return doToDto(entity);
    }

    @Override
    public final ENTITY toEntity(DTO dto) {
        if (dto == null) return null;
        return doToEntity(dto);
    }

    protected DTO doToDto(ENTITY entity) {
        return modelMapper.map(entity, getDtoClass());
    }

    protected ENTITY doToEntity(DTO dto) {
        return modelMapper.map(dto, getEntityClass());
    }

    protected Class<DTO> getDtoClass() {
        return ClassUtils.getClassParametrizedType(getClass(), 1);
    }

    protected Class<ENTITY> getEntityClass() {
        return ClassUtils.getClassParametrizedType(getClass(), 0);
    }

    /**
     * Subclasses can override this method to configure the model mapper.
     *
     * @param modelMapper the model mapper
     */
    protected void configureMapper(ModelMapper modelMapper) {
    }

    @Override
    public void afterPropertiesSet() {
        configureMapper(modelMapper);
    }
}
