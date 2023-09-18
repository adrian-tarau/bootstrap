package net.microfalx.bootstrap.web.dataset;

import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.dataset.annotation.Searchable;
import net.microfalx.bootstrap.model.*;
import net.microfalx.bootstrap.web.component.Button;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Toolbar;
import net.microfalx.bootstrap.web.controller.NavigableController;
import net.microfalx.bootstrap.web.template.tools.DataSetTool;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.StreamResource;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.microfalx.lang.StringUtils.*;

/**
 * Base class for all data set controllers.
 */
public abstract class DataSetController<M, ID> extends NavigableController<M, ID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetController.class);

    private static final String MESSAGE_ATTR = "message";
    private static final String INVALID_FILTER_PREFIX = "Invalid filter: ";
    private static final String DATE_RANGE_SEPARATOR = "|";
    private static final String BROWSE_VIEW = "dataset/browse";
    private static final String FRAGMENT_SEPARATOR = "::";

    @Autowired
    private DataSetService dataSetService;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    @GetMapping()
    public final String browse(Model model,
                               @RequestParam(value = "page", defaultValue = "0") int pageParameter,
                               @RequestParam(value = "range", defaultValue = "") String rangeParameter,
                               @RequestParam(value = "query", defaultValue = "") String queryParameter,
                               @RequestParam(value = "sort", defaultValue = "") String sortParameter) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "browse", pageParameter, rangeParameter, queryParameter, sortParameter);
        updateModel(dataSet, model, State.BROWSE);
        updateModel(dataSet, model, null, State.BROWSE);
        processParams(dataSet, model, pageParameter, rangeParameter, queryParameter, sortParameter);
        return BROWSE_VIEW;
    }

    @GetMapping("page")
    public final String next(Model model, HttpServletResponse response,
                             @RequestParam(value = "page", defaultValue = "0") int pageParameter,
                             @RequestParam(value = "range", defaultValue = "") String rangeParameter,
                             @RequestParam(value = "query", defaultValue = "") String queryParameter,
                             @RequestParam(value = "sort", defaultValue = "") String sortParameter) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "next page", pageParameter, rangeParameter, queryParameter, sortParameter);
        updateModel(dataSet, model, State.BROWSE);
        Page<M> page = processParams(dataSet, model, pageParameter, rangeParameter, queryParameter, sortParameter);
        response.addHeader("X-DATASET-PAGE-INFO", DataSetTool.getPageInfo(page));
        response.addHeader("X-DATASET-PAGE-INFO-EXTENDED", DataSetTool.getPageAndRecordInfo(page));
        if (model.containsAttribute(MESSAGE_ATTR))
            response.addHeader("X-DATASET-MESSAGE", (String) model.getAttribute(MESSAGE_ATTR));
        return "dataset/page::#dataset-page";
    }

    @GetMapping("{id}/add")
    public final String add(Model model, @PathVariable("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "add", 0, null, null, null);
        updateModel(dataSet, model, State.ADD);
        updateModel(dataSet, model, null, State.ADD);
        if (beforeAdd(dataSet, model)) {
            return "dataset/add::#dataset-modal";
        } else {
            return BROWSE_VIEW;
        }
    }

    @GetMapping("{id}/view")
    public final String view(Model model, @PathVariable("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "view", 0, null, null, null);
        updateModel(dataSet, model, State.VIEW);
        M dataSetModel = findModel(dataSet, model, id, State.VIEW);
        beforeView(dataSet, model, dataSetModel);
        return "dataset/view::#dataset-modal";
    }

    @GetMapping("{id}/edit")
    public final String edit(Model model, @PathVariable("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "edit", 0, null, null, null);
        updateModel(dataSet, model, State.EDIT);
        M dataSetModel = findModel(dataSet, model, id, State.EDIT);
        if (beforeEdit(dataSet, model, dataSetModel)) {
            return "dataset/view::#dataset-modal";
        } else {
            return BROWSE_VIEW;
        }
    }

    @GetMapping("{id}/delete")
    public final String delete(Model model, @PathParam("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "delete", 0, null, null, null);
        M dataSetModel = findModel(dataSet, model, id, State.BROWSE);
        beforeDelete(dataSet, model, dataSetModel);
        return "dataset/browse";
    }

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public final void upload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "upload", 0, null, null, null);
        upload(dataSet, model, StreamResource.create(file.getInputStream(), file.getOriginalFilename()));
        String message = "File '" + file.getOriginalFilename() + "' was successfully uploaded";
        model.addAttribute("message", message);
    }

    @GetMapping(value = "{id}/download")
    @ResponseBody()
    public final ResponseEntity<InputStreamResource> download(Model model, @PathVariable("id") String id) throws IOException {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        log(dataSet, "download", 0, null, null, null);
        M dataSetModel = findModel(dataSet, model, id, State.BROWSE);
        Resource resource = download(dataSet, model, dataSetModel);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(resource.getMimeType()))
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFileName() + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }

    /**
     * Updates the toolbar with additional actions.
     *
     * @param toolbar the toolbar
     */
    protected void updateToolbar(Toolbar toolbar) {
        // empty on purpose
    }

    /**
     * Updates the actions.
     *
     * @param menu the menu
     */
    protected void updateActions(Menu menu) {
        // empty on purpose
    }

    /**
     * Subclasses can update the controller model with additional variables.
     *
     * @param dataSet         the data set
     * @param controllerModel the model used by controller/template
     * @param dataSetModel    the model return by the data set, will be present only for {@link State#EDIT} and {@link State#VIEW}.
     * @param state           the state of the data set
     */
    protected void updateModel(DataSet<M, Field<M>, ID> dataSet, Model controllerModel, M dataSetModel, State state) {
        // empty by default
    }

    /**
     * Subclasses can use this method to handle a upload request file and provisioning (or update) an entry.
     *
     * @param dataSet  the data set
     * @param model    the model used by controller/template
     * @param resource the content of the uploaded file
     */
    protected void upload(DataSet<M, Field<M>, ID> dataSet, Model model, Resource resource) {
        throw new DataSetException("Upload is not supported for " + dataSet.getName());
    }

    /**
     * Subclasses can use this method to handle a download request to download the content behind a data set model.
     *
     * @param dataSet         the data set
     * @param controllerModel the model used by controller/template
     * @param dataSetModel    the model return by the data set, will be present only for {@link State#EDIT} and {@link State#VIEW}.
     */
    protected Resource download(DataSet<M, Field<M>, ID> dataSet, Model controllerModel, M dataSetModel) {
        throw new DataSetException("Download is not supported for " + dataSet.getName());
    }

    /**
     * Invoked before rendering each model during browse.
     *
     * @param dataSet         the data set
     * @param controllerModel the model associated with the controller
     * @param dataSetModel    the data set model for the selected row
     * @return {@code true} to continue the add action, {@code false} otherwise
     */
    protected boolean beforeBrowse(DataSet<M, Field<M>, ID> dataSet, Model controllerModel, M dataSetModel) {
        return true;
    }

    /**
     * Invoked before the view is rendered.
     *
     * @param dataSet         the data set
     * @param controllerModel the model associated with the controller
     * @param dataSetModel    the data set model for the selected entry
     */
    protected void beforeView(DataSet<M, Field<M>, ID> dataSet, Model controllerModel, M dataSetModel) {
        // empty by default
    }

    /**
     * Invoked before the add view is displayed.
     *
     * @param dataSet         the data set
     * @param controllerModel the model associated with the controller
     * @return {@code true} to continue the add action, {@code false} otherwise
     */
    protected boolean beforeAdd(DataSet<M, Field<M>, ID> dataSet, Model controllerModel) {
        return true;
    }

    /**
     * Invoked before the edit view is displayed.
     *
     * @param dataSet         the data set
     * @param controllerModel the model associated with the controller
     * @param dataSetModel    the data set model for the selected entry
     * @return {@code true} to continue the add action, {@code false} otherwise
     */
    protected boolean beforeEdit(DataSet<M, Field<M>, ID> dataSet, Model controllerModel, M dataSetModel) {
        return true;
    }

    /**
     * Invoked before the delete is performed.
     *
     * @param dataSet         the data set
     * @param controllerModel the model associated with the controller
     * @param dataSetModel    the data set model for the selected entry
     * @return {@code true} to continue the add action, {@code false} otherwise
     */
    protected boolean beforeDelete(DataSet<M, Field<M>, ID> dataSet, Model controllerModel, M dataSetModel) {
        return true;
    }

    /**
     * Returns the data set used with this controller.
     *
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    protected final DataSet<M, Field<M>, ID> getDataSet() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnot = getDataSetAnnotation();
        return dataSetService.lookup((Class<M>) dataSetAnnot.model(), this);
    }

    private Pageable getPage(int page, Sort sort) {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        return PageRequest.of(page, dataSetAnnotation.pageSize(), sort);
    }

    private net.microfalx.bootstrap.dataset.annotation.DataSet getDataSetAnnotation() {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnot = AnnotationUtils.getAnnotation(this, net.microfalx.bootstrap.dataset.annotation.DataSet.class);
        if (dataSetAnnot == null) {
            throw new DataSetException("A @DataSet annotation could not be located for controller " + ClassUtils.getName(this));
        }
        return dataSetAnnot;
    }

    private Page<M> extractModels(Filter filter, Pageable pageable) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        return dataSet.findAll(pageable, filter);
    }

    private Sort getSort(String value) {
        if (isEmpty(value)) return getDefaultSort();
        List<Sort.Order> orders = new ArrayList<>();
        String[] parts = StringUtils.split(value, ";");
        for (String part : parts) {
            String[] parts2 = StringUtils.split(part, "=");
            if (parts2.length > 2) throw new IllegalArgumentException("Invalid sorting value ( "
                    + value + "), expected format: FIELD_NAME1[=DIRECTION];FIELD_NAME2[=DIRECTION]");
            Sort.Direction direction = Sort.Direction.ASC;
            if (parts2.length == 2) {
                direction = "asc".equalsIgnoreCase(parts2[1]) ? Sort.Direction.ASC : "desc".equalsIgnoreCase(parts2[1]) ? Sort.Direction.DESC : null;
            }
            if (direction != null) {
                orders.add(new Sort.Order(direction, parts2[0]));
            }
        }
        return Sort.by(orders);
    }

    private Sort getDefaultSort() {
        List<Sort.Order> orders = new ArrayList<>();
        Metadata<M, Field<M>, ID> metadata = getDataSet().getMetadata();
        for (Field<M> field : metadata.getFields()) {
            OrderBy orderByAnnot = field.findAnnotation(OrderBy.class);
            if (orderByAnnot != null) {
                orders.add(Sort.Order.by(field.getName())
                        .with(orderByAnnot.value() == OrderBy.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC));
            }
        }
        if (orders.isEmpty()) {
            for (Field<M> field : metadata.getNameFields()) {
                orders.add(Sort.Order.by(field.getName()));
            }
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private Toolbar getToolBar(DataSet<M, Field<M>, ID> dataSet) {
        Toolbar toolbar = new Toolbar().setId("toolbar");
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        if (!dataSet.isReadOnly() && dataSetAnnotation.canAdd()) {
            toolbar.add(new Button().setAction("add").setText("Add").setIcon("fa-solid fa-plus").setPosition(1));
        }
        if (!dataSet.isReadOnly() && dataSetAnnotation.canUpload()) {
            toolbar.add(new Button().setAction("upload").setText("Upload").setIcon("fa-solid fa-upload")
                    .setCssClass("dataset-drop-zone").setPosition(2));
        }
        // if (toolbar.hasChildren()) toolbar.add(new Separator());
        toolbar.add(new Button().setAction("print").setText("Print").setIcon("fa-solid fa-print").setPosition(100));
        //toolbar.add(new Separator());
        toolbar.add(new Button().setAction("refresh").setText("Refresh").setIcon("fa-solid fa-arrows-rotate").setPosition(200));
        updateToolbar(toolbar);
        return toolbar;
    }

    private Menu getMenu(DataSet<M, Field<M>, ID> dataSet) {
        Menu menu = new Menu().setId("actions");
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        if (!dataSet.isReadOnly()) {
            menu.add(new Item().setAction("view").setText("View").setIcon("fa-solid fa-eye"));
            menu.add(new Item().setAction("edit").setText("Edit").setIcon("fa-solid fa-pen-to-square"));
            if (dataSetAnnotation.canDelete()) {
                menu.add(new Item().setAction("delete").setText("Delete").setIcon("fa-solid fa-trash-can"));
            }
            if (dataSetAnnotation.canDownload()) {
                menu.add(new Item().setAction("download").setText("Download").setIcon("fa-solid fa-download"));
            }
        }
        updateActions(menu);
        return menu;
    }

    private void updateModel(DataSet<M, Field<M>, ID> dataSet, Model model, State state) {
        dataSet.setState(state);
        model.addAttribute("controller", this);
        model.addAttribute("dataset", dataSet);
        model.addAttribute("metadata", dataSet.getMetadata());
        model.addAttribute("toolbar", getToolBar(dataSet));
        model.addAttribute("actions", getMenu(dataSet));
        model.addAttribute("model", null);
        updateModelTemplate(dataSet, model);
    }

    private void updateModelTemplate(DataSet<M, Field<M>, ID> dataSet, Model model) {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        String fieldsTemplate = "fragments/dataset";
        String fieldsFragment = "fields";
        if (dataSet.getState() == State.VIEW) {
            fieldsTemplate = getTemplateReference(dataSetAnnotation.viewTemplate(), fieldsTemplate, 0);
            fieldsFragment = getTemplateReference(dataSetAnnotation.viewTemplate(), fieldsFragment, 1);
        }
        model.addAttribute("fieldsTemplate", fieldsTemplate);
        model.addAttribute("fieldsFragment", fieldsFragment);
        String detailTemplate = null;
        String detailFragment = "fields";
        if (dataSet.getState() == State.BROWSE) {
            detailTemplate = getTemplateReference(dataSetAnnotation.detailTemplate(), detailTemplate, 0);
            detailFragment = getTemplateReference(dataSetAnnotation.detailTemplate(), detailFragment, 1);
        }
        model.addAttribute("detailTemplate", detailTemplate);
        model.addAttribute("detailFragment", detailFragment);
        if (ObjectUtils.isNotEmpty(dataSetAnnotation.viewClasses())) {
            model.addAttribute("viewClasses", StringUtils.join(" ", dataSetAnnotation.viewClasses()));
        } else {
            model.addAttribute("viewClasses", StringUtils.EMPTY_STRING);
        }
    }

    private M findModel(DataSet<M, Field<M>, ID> dataSet, Model model, String id, State state) {
        TransactionTemplate transactionTemplate = getTransactionTemplate(dataSet);
        if (transactionTemplate != null) {
            return transactionTemplate.execute(status -> doFindModel(dataSet, model, id, state));
        } else {
            return doFindModel(dataSet, model, id, state);
        }
    }

    private M doFindModel(DataSet<M, Field<M>, ID> dataSet, Model model, String id, State state) {
        CompositeIdentifier<M, Field<M>, ID> compositeId = dataSet.getMetadata().getId(id);
        Optional<M> result = dataSet.findById(compositeId.toId());
        if (result.isPresent()) {
            M dataSetModel = result.get();
            updateModel(dataSet, model, dataSetModel, state);
            model.addAttribute("model", dataSetModel);
            return dataSetModel;
        } else {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "A model with identifier '" + id + "' does not exist");
        }
    }

    private TransactionTemplate getTransactionTemplate(DataSet<M, Field<M>, ID> dataSet) {
        Metadata<M, Field<M>, ID> metadata = dataSet.getMetadata();
        if (transactionManager != null && metadata.hasAnnotation(Entity.class)) {
            return new TransactionTemplate(transactionManager);
        } else {
            return null;
        }
    }

    private Page<M> processParams(DataSet<M, Field<M>, ID> dataSet, Model model,
                                  int pageParameter, String rangeParameter, String queryParameter, String sortParameter) {
        Sort sort = getSort(sortParameter);
        Pageable page = getPage(pageParameter, sort);
        Filter filter = getFilter(dataSet, model, rangeParameter, queryParameter);
        Page<M> pagedModels = extractModels(filter, page);
        model.addAttribute("page", pagedModels);
        model.addAttribute("query", defaultIfEmpty(queryParameter, getDefaultQuery(dataSet)));
        model.addAttribute("sort", sort);
        model.addAttribute("index", new MutableLong(pagedModels.getPageable().getOffset() + 1));
        model.addAttribute("hasTimeRange", hasTimeRange(dataSet));
        return pagedModels;
    }

    private String getDefaultQuery(DataSet<M, Field<M>, ID> dataSet) {
        net.microfalx.bootstrap.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        return dataSetAnnotation.defaultQuery();
    }

    private Filter getFilter(DataSet<M, Field<M>, ID> dataSet, Model model, String rangeParameter, String queryParameter) {
        String defaultQuery = getDefaultQuery(dataSet);
        if (isNotEmpty(defaultQuery)) {
            QueryParser<M, Field<M>, ID> queryParser = createQueryParser(dataSet, defaultQuery);
            if (!queryParser.isValid()) {
                defaultQuery = null;
                LOGGER.warn("Default query is not valid: " + queryParser.validate());
            }
        }
        Filter filter = doGetFilter(dataSet, model, defaultIfEmpty(queryParameter, defaultQuery));
        if (filter == null) filter = doGetFilter(dataSet, model, defaultQuery);
        return addRangeFilter(dataSet, model, rangeParameter, filter);
    }

    private Filter doGetFilter(DataSet<M, Field<M>, ID> dataSet, Model model, String query) {
        Filter filter = Filter.create();
        QueryParser<M, Field<M>, ID> queryParser = createQueryParser(dataSet, query);
        if (queryParser.isValid()) {
            filter = Filter.create(queryParser.parse());
        } else {
            String reason = queryParser.validate();
            model.addAttribute("message", INVALID_FILTER_PREFIX + reason);
            LOGGER.warn("Failed to parse query '" + query + "', reason: " + reason);
        }
        try {
            dataSet.validate(filter);
        } catch (Exception e) {
            filter = null;
            String reason = e.getMessage();
            model.addAttribute("message", INVALID_FILTER_PREFIX + reason);
            String message = "Failed to validate query '" + query + "', reason: " + reason;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(message, e);
            } else {
                LOGGER.warn(message);
            }
        }
        return filter;
    }

    private Filter addRangeFilter(DataSet<M, Field<M>, ID> dataSet, Model model, String rangeParameter, Filter filter) {
        if (!hasTimeRange(dataSet)) return filter;
        String[] rangeParts = split(rangeParameter, DATE_RANGE_SEPARATOR);
        if (rangeParts.length == 0) rangeParts = getDefaultRange(dataSet);
        if (rangeParts.length == 0) return filter;
        if (!(rangeParts.length == 1 || rangeParts.length == 2)) {
            model.addAttribute("message", INVALID_FILTER_PREFIX + "date/time range requires two components");
        } else {
            Field<M> timestampField = dataSet.getMetadata().findTimestampField();
            ZonedDateTime startTime;
            ZonedDateTime endTime;
            if (rangeParts.length == 1) {
                startTime = Field.from(rangeParts[0], ZonedDateTime.class);
                endTime = atEndOfDay(startTime);
            } else {
                startTime = Field.from(rangeParts[0], ZonedDateTime.class);
                endTime = Field.from(rangeParts[1], ZonedDateTime.class);
            }
            model.addAttribute("daterange", startTime.toString() + "|" + endTime.toString());
            Expression timeExpression = ComparisonExpression.between(timestampField.getName(), startTime, endTime);
            LogicalExpression finalExpression = LogicalExpression.and(timeExpression, filter.getExpression());
            filter = Filter.create(finalExpression, filter.getOffset(), filter.getLimit());
        }
        return filter;
    }

    private List<String> getStringFields(DataSet<M, Field<M>, ID> dataSet) {
        return dataSet.getMetadata().getFields(Field.DataType.STRING).stream()
                .filter(this::isSearchable).map(Field::getName)
                .toList();
    }

    private boolean isSearchable(Field<M> field) {
        Searchable searchableAnnot = field.findAnnotation(Searchable.class);
        if (searchableAnnot != null) {
            return searchableAnnot.value();
        } else {
            return true;
        }
    }

    private String[] getDefaultRange(DataSet<M, Field<M>, ID> dataSet) {
        String[] range = EMPTY_STRING_ARRAY;
        String[] defaultRange = getDataSetAnnotation().range();
        if (defaultRange.length > 0) {
            range = new String[2];
            if (defaultRange.length == 1) {
                range[0] = Field.from(defaultRange[0], ZonedDateTime.class).toString();
                range[1] = atEndOfDay(Field.from(defaultRange[0], ZonedDateTime.class)).toString();
            } else if (defaultRange.length == 2) {
                range[0] = Field.from(defaultRange[0], ZonedDateTime.class).toString();
                range[1] = atEndOfDay(Field.from(defaultRange[1], ZonedDateTime.class)).toString();
            }
        }
        return range;
    }

    private boolean hasTimeRange(DataSet<M, Field<M>, ID> dataSet) {
        return dataSet.getMetadata().findTimestampField() != null;
    }

    private QueryParser<M, Field<M>, ID> createQueryParser(DataSet<M, Field<M>, ID> dataSet, String query) {
        return new QueryParser<>(dataSet.getMetadata(), query)
                .addDefaultFields(getStringFields(dataSet));
    }

    private String getTemplateReference(String template, String defaultValue, int index) {
        if (isEmpty(template)) return defaultValue;
        String[] parts = splitTemplateReferences(template);
        if (index >= parts.length) return defaultValue;
        return parts[index].trim();
    }

    private String[] splitTemplateReferences(String template) {
        if (isEmpty(template)) return EMPTY_STRING_ARRAY;
        int index = template.indexOf(FRAGMENT_SEPARATOR);
        if (index == -1) return new String[]{template};
        String[] parts = new String[2];
        parts[0] = template.substring(0, index).trim();
        parts[1] = template.substring(index + 2).trim();
        return parts;
    }

    private ZonedDateTime atEndOfDay(ZonedDateTime dateTime) {
        return dateTime.plusDays(1).minusSeconds(1);
    }

    private void log(DataSet<M, Field<M>, ID> dataSet, String action, int page,
                     String range, String query, String sort) {
        range = defaultIfEmpty(range, "<empty>");
        query = defaultIfEmpty(query, "<empty>");
        sort = defaultIfEmpty(sort, "<empty>");
        LOGGER.debug("{} data set {}, page {}, range{}, query {}, sort {}", capitalizeWords(action), dataSet.getName(),
                page, range, query, sort);
    }
}
