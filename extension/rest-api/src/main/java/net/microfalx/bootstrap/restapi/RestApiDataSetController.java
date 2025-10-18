package net.microfalx.bootstrap.restapi;

import net.microfalx.bootstrap.dataset.AbstractCrudController;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.lang.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all REST API controllers which manage a dataset (a resource with CRUD).
 * <p>
 * Subclasses must annotate the controller with {@link net.microfalx.bootstrap.dataset.annotation.DataSet} to indicate which dataset they manage.
 *
 * @param <DTO>    the data transfer object type
 * @param <ENTITY> the entity type
 * @param <ID>     the record  identifier type
 */
public abstract class RestApiDataSetController<ENTITY, DTO, ID> extends RestApiModelController<ENTITY, DTO, ID> {

    private RestApiCrudController<ENTITY, DTO, ID> cachedCrudController;

    private Map<String, String> validationErrors = new HashMap<>();

    /**
     * Lists DTOs with optional search and paging.
     *
     * @param timeRange the time range parameter
     * @param query     the query parameter
     * @param sort      the sort parameter
     * @param page      the page parameter
     * @param pageSize  the size of one page parameter
     * @return a non-null instance
     */
    protected final List<DTO> doList(String timeRange, String query, String sort, int page, int pageSize) {
        getCrudController().logRequest("list", timeRange, query, sort, page);
        Page<ENTITY> entities = getCrudController().getPage(timeRange, query, sort, page);
        return entities.stream().map(this::toDto).toList();
    }

    /**
     * Finds a single DTO by its unique identifier.
     *
     * @param id the unique identifier
     * @return the DTO or null if not found
     */
    protected final DTO doFind(ID id) {
        RestApiCrudController<ENTITY, DTO, ID> controller = getCrudController();
        controller.throwIdentifierRequired(id);
        controller.logRequest("find", null, null, null, 0);
        ENTITY entity = controller.findModel(id, State.VIEW);
        if (entity == null) controller.throwModelNotFound(ObjectUtils.toString(id));
        return toDto(entity);
    }

    /**
     * Finds a single DTO by its unique identifier.
     *
     * @param id the unique identifier
     * @return the DTO or null if not found
     */
    protected final DTO doFind(String id) {
        RestApiCrudController<ENTITY, DTO, ID> controller = getCrudController();
        controller.throwIdentifierRequired(id);
        controller.logRequest("find", null, null, null, 0);
        ENTITY entity = controller.findModel(id, State.VIEW);
        if (entity == null) controller.throwModelNotFound(id);
        return toDto(entity);
    }

    /**
     * Adds a new DTO.
     *
     * @param dto the DTO
     */
    protected final void doCreate(DTO dto) {
        RestApiCrudController<ENTITY, DTO, ID> controller = getCrudController();
        controller.logRequest("create", null, null, null, 0);
        validate(dto, State.ADD, validationErrors);
        ENTITY entity = toEntity(dto);
        controller.persist(entity, State.ADD);
    }

    /**
     * Updates an existing DTO.
     *
     * @param dto the DTO
     */
    protected final void doUpdate(DTO dto) {
        RestApiCrudController<ENTITY, DTO, ID> controller = getCrudController();
        controller.logRequest("update", null, null, null, 0);
        validate(dto, State.EDIT, validationErrors);
        ENTITY entity = toEntity(dto);
        controller.persist(entity, State.EDIT);
    }

    /**
     * Deletes an existing DTO.
     *
     * @param dto the DTO
     */
    protected final void doDeleteByModel(DTO dto) {
        RestApiCrudController<ENTITY, DTO, ID> controller = getCrudController();
        controller.logRequest("delete", null, null, null, 0);
        ENTITY entity = toEntity(dto);
        controller.deleteByModel(entity);
    }

    /**
     * Deletes an existing DTO.
     *
     * @param id the unique identifier
     */
    protected final void doDeleteById(Object id) {
        RestApiCrudController<ENTITY, DTO, ID> controller = getCrudController();
        controller.logRequest("delete", null, null, null, 0);
        controller.deleteById(id);
    }

    /**
     * Subclasses can update entity with additional variables and/or change the data set model.
     *
     * @param dataSet the data set
     * @param model   the model return by the data set, will be present only for {@link State#EDIT} and {@link State#VIEW}.
     * @param state   the state of the data set
     */
    protected void updateModel(DataSet<ENTITY, Field<ENTITY>, ID> dataSet, ENTITY model, State state) {
        // empty by default
    }

    /**
     * Subclasses can update the entity with additional variables related to a data set page.
     *
     * @param dataSet the data set
     * @param page    the page of models
     * @param filter  the filter extracted from request
     * @param sort    the sort extracted from request
     */
    protected void updatePage(DataSet<ENTITY, Field<ENTITY>, ID> dataSet, Page<ENTITY> page, Filter filter, Sort sort) {
        // empty by default
    }

    /**
     * Invoked before the entity is persisted.
     * <p>
     * If the model is supported by a database, the event is called under a transaction.
     *
     * @param dataSet the data set
     * @param model   the model
     * @param state   the data set state
     */
    protected void beforePersist(DataSet<ENTITY, Field<ENTITY>, ID> dataSet, ENTITY model, State state) {
        // empty by default
    }

    /**
     * Invoked after the entity is persisted.
     * <p>
     * If the model is supported by a database, the event is called under a transaction after the model is saved.
     *
     * @param dataSet the data set
     * @param model   the model
     * @param state   the data set state
     */
    protected void afterPersist(DataSet<ENTITY, Field<ENTITY>, ID> dataSet, ENTITY model, State state) {
        // empty by default
    }

    private RestApiCrudController<ENTITY, DTO, ID> getCrudController() {
        if (cachedCrudController == null) {
            cachedCrudController = new RestApiCrudController<>(getDataSetService(), this);
        }
        return cachedCrudController;
    }

    private static class RestApiCrudController<ENTITY, DTO, ID> extends AbstractCrudController<ENTITY, ID, RestApiDataSetController<ENTITY, DTO, ID>> {


        public RestApiCrudController(DataSetService dataSetService, RestApiDataSetController<ENTITY, DTO, ID> owner) {
            super(dataSetService, owner);
        }

        @Override
        protected void updateModel(net.microfalx.bootstrap.dataset.DataSet<ENTITY, Field<ENTITY>, ID> dataSet, ENTITY model, State state) {
            getOwner().updateModel(dataSet, model, state);
        }

        @Override
        protected void updatePage(net.microfalx.bootstrap.dataset.DataSet<ENTITY, Field<ENTITY>, ID> dataSet, Page<ENTITY> page, Filter filter, Sort sort) {
            getOwner().updatePage(dataSet, page, filter, sort);
        }

        @Override
        protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<ENTITY, Field<ENTITY>, ID> dataSet, ENTITY model, State state) {
            getOwner().beforePersist(dataSet, model, state);
        }

        @Override
        protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<ENTITY, Field<ENTITY>, ID> dataSet, ENTITY model, State state) {
            getOwner().afterPersist(dataSet, model, state);
        }
    }

}
