package net.microfalx.bootstrap.web.dataset.controller;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Filter;
import net.microfalx.bootstrap.web.controller.NavigableController;
import net.microfalx.bootstrap.web.dataset.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetException;
import net.microfalx.bootstrap.web.dataset.DataSetService;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all data set controllers.
 */
public abstract class DataSetController<M, ID> extends NavigableController<M, ID> {

    @Autowired
    private DataSetService dataSetService;

    private DataSet<M, Field<M>, ID> dataSet;

    @GetMapping()
    public String browse(Model model,
                         @RequestParam(value = "page", defaultValue = "0") int pageParameter,
                         @RequestParam(value = "query", defaultValue = "") String queryParameter,
                         @RequestParam(value = "filter", defaultValue = "") String filterParameter,
                         @RequestParam(value = "sort", defaultValue = "") String sortParameter) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        Filter filter = getFilter(filterParameter);
        Sort sort = getSort(sortParameter);
        Pageable page = getPage(pageParameter, sort);
        Page<M> models = extractModels(filter, page);
        model.addAttribute("dataset", dataSet);
        model.addAttribute("page", models);
        model.addAttribute("query", queryParameter);
        model.addAttribute("sort", sort);
        model.addAttribute("metadata", dataSet.getMetadata());
        return "dataset/browse";
    }

    /**
     * Returns the data set used with this controller.
     *
     * @return a non-null instance
     */
    @SuppressWarnings("unchecked")
    protected final DataSet<M, Field<M>, ID> getDataSet() {
        if (dataSet == null) {
            net.microfalx.bootstrap.web.dataset.annotation.DataSet dataSetAnnot = getDataSetAnnotation();
            dataSet = dataSetService.lookup((Class<M>) dataSetAnnot.model(), this);
        }
        return dataSet;
    }

    private Pageable getPage(int page, Sort sort) {
        net.microfalx.bootstrap.web.dataset.annotation.DataSet dataSetAnnotation = getDataSetAnnotation();
        return PageRequest.of(page, dataSetAnnotation.pageSize(), sort);
    }

    private net.microfalx.bootstrap.web.dataset.annotation.DataSet getDataSetAnnotation() {
        net.microfalx.bootstrap.web.dataset.annotation.DataSet dataSetAnnot = AnnotationUtils.getAnnotation(this, net.microfalx.bootstrap.web.dataset.annotation.DataSet.class);
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
        if (StringUtils.isEmpty(value)) return Sort.unsorted();
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
}
