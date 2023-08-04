package net.microfalx.bootstrap.web.dataset;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import net.microfalx.bootstrap.dataset.DataSet;
import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.model.CompositeIdentifier;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.web.component.Button;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.component.Toolbar;
import net.microfalx.bootstrap.web.controller.NavigableController;
import net.microfalx.bootstrap.web.template.tools.DataSetTool;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base class for all data set controllers.
 */
public abstract class DataSetController<M, ID> extends NavigableController<M, ID> {

    @Autowired
    private DataSetService dataSetService;

    @GetMapping()
    public String browse(Model model,
                         @RequestParam(value = "page", defaultValue = "0") int pageParameter,
                         @RequestParam(value = "query", defaultValue = "") String queryParameter,
                         @RequestParam(value = "filter", defaultValue = "") String filterParameter,
                         @RequestParam(value = "sort", defaultValue = "") String sortParameter) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        updateModel(dataSet, model, State.BROWSE);
        processParams(dataSet, model, pageParameter, queryParameter, filterParameter, sortParameter);
        return "dataset/browse";
    }

    @GetMapping(path = "page")
    public String next(Model model, HttpServletResponse response,
                       @RequestParam(value = "page", defaultValue = "0") int pageParameter,
                       @RequestParam(value = "query", defaultValue = "") String queryParameter,
                       @RequestParam(value = "filter", defaultValue = "") String filterParameter,
                       @RequestParam(value = "sort", defaultValue = "") String sortParameter) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        updateModel(dataSet, model, State.BROWSE);
        Page<M> page = processParams(dataSet, model, pageParameter, queryParameter, filterParameter, sortParameter);
        response.addHeader("X-DATASET-PAGE-INFO", DataSetTool.getPageInfo(page));
        response.addHeader("X-DATASET-PAGE-INFO-EXTENDED", DataSetTool.getPageAndRecordInfo(page));
        return "dataset/page:: #dataset-page";
    }

    @GetMapping(path = "{id}/add")
    public String add(Model model, @PathVariable("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        updateModel(dataSet, model, State.ADD);
        return "dataset/add:: #dataset-modal";
    }

    @GetMapping(path = "{id}/view")
    public String view(Model model, @PathVariable("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        updateModel(dataSet, model, State.VIEW);
        findModel(dataSet, model, id);
        return "dataset/view :: #dataset-modal";
    }

    @GetMapping(path = "{id}/edit")
    public String edit(Model model, @PathVariable("id") String id) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        updateModel(dataSet, model, State.EDIT);
        findModel(dataSet, model, id);
        return "dataset/view:: #dataset-modal";
    }

    @GetMapping(path = "{id}/delete")
    public String delete(Model model, @PathParam("id") String id) {
        return "dataset/browse";
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

    private Filter getFilter(String filterParameter) {
        return null;
    }

    private Sort getSort(String value) {
        if (StringUtils.isEmpty(value)) return getDefaultSort();
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
        if (!dataSet.isReadOnly()) {
            toolbar.add(new Button().setText("Add").setIcon("fa-solid fa-plus"));
        }
        // if (toolbar.hasChildren()) toolbar.add(new Separator());
        toolbar.add(new Button().setText("Print").setIcon("fa-solid fa-print"));
        toolbar.add(new Button().setText("Export").setIcon("fa-solid fa-file-export"));
        //toolbar.add(new Separator());
        toolbar.add(new Button().setText("Refresh").setIcon("fa-solid fa-arrows-rotate"));
        updateToolbar(toolbar);
        return toolbar;
    }

    private Menu getMenu(DataSet<M, Field<M>, ID> dataSet) {
        Menu menu = new Menu().setId("actions");
        if (!dataSet.isReadOnly()) {
            menu.add(new Item().setAction("view").setText("View").setIcon("fa-solid fa-eye"));
            menu.add(new Item().setAction("edit").setText("Edit").setIcon("fa-solid fa-pen-to-square"));
            menu.add(new Item().setAction("delete").setText("Delete").setIcon("fa-solid fa-trash-can"));
        }
        updateActions(menu);
        return menu;
    }

    private void updateModel(DataSet<M, Field<M>, ID> dataSet, Model model, State state) {
        dataSet.setState(state);
        model.addAttribute("dataset", dataSet);
        model.addAttribute("metadata", dataSet.getMetadata());
        model.addAttribute("toolbar", getToolBar(dataSet));
        model.addAttribute("actions", getMenu(dataSet));
        model.addAttribute("model", null);
    }

    private void findModel(DataSet<M, Field<M>, ID> dataSet, Model model, String id) {
        CompositeIdentifier<M, Field<M>, ID> compositeId = dataSet.getMetadata().getId(id);
        Optional<M> dataSetModel = dataSet.findById(compositeId.toId());
        if (dataSetModel.isPresent()) {
            model.addAttribute("model", dataSetModel.get());
        } else {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "A model with identifier '" + id + "' does not exist");
        }
    }

    private Page<M> processParams(DataSet<M, Field<M>, ID> dataSet, Model model,
                                  int pageParameter, String queryParameter, String filterParameter, String sortParameter) {
        Filter filter = getFilter(filterParameter);
        Sort sort = getSort(sortParameter);
        Pageable page = getPage(pageParameter, sort);
        Page<M> pagedModels = extractModels(filter, page);
        model.addAttribute("page", pagedModels);
        model.addAttribute("query", queryParameter);
        model.addAttribute("sort", sort);
        model.addAttribute("index", new MutableLong(pagedModels.getPageable().getOffset() + 1));
        return pagedModels;
    }
}
