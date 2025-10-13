package net.microfalx.bootstrap.restapi;

/**
 * A mapper which maps between a source and a destination models.
 * <p>
 * The intent is to provide a mapping between an entity and a DTO based on annotations using <a href="https://mapstruct.org/">MapStruct</a>.
 *
 * @param <S>  the source record type (usually an entity)
 * @param <D>  the target record type (usually a DTO)
 */
public interface RestApiMapper<S, D> {

    /**
     * Maps a source model to a destination model.
     *
     * @param entity the source model (usually an entity)
     * @return the destination model (usually a DTO)
     */
    D toDto(S entity);

    /**
     * Maps a destination model to a source model.
     *
     * @param dto the destination model (usually a DTO)
     * @return the source model (usually an entity)
     */
    S toEntity(D dto);
}
