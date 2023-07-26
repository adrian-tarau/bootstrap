package net.microfalx.bootstrap.web.dataset.controller;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.controller.NavigableController;
import net.microfalx.bootstrap.web.dataset.DataSet;
import net.microfalx.bootstrap.web.dataset.DataSetException;
import net.microfalx.bootstrap.web.dataset.DataSetService;
import net.microfalx.lang.AnnotationUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Base class for all data set controllers.
 */
public abstract class DataSetController<M, ID> extends NavigableController<M, ID> {

    @Autowired
    private DataSetService dataSetService;

    private DataSet<M, Field<M>, ID> dataSet;

    @GetMapping()
    public String browse(Model model) {
        DataSet<M, Field<M>, ID> dataSet = getDataSet();
        model.addAttribute("dataset", dataSet);
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
            net.microfalx.bootstrap.web.dataset.annotation.DataSet dataSetAnnot = AnnotationUtils.getAnnotation(this, net.microfalx.bootstrap.web.dataset.annotation.DataSet.class);
            if (dataSetAnnot == null) {
                throw new DataSetException("A @DataSet annotation could not be located for controller " + ClassUtils.getName(this));
            }
            dataSet = dataSetService.lookup((Class<M>) dataSetAnnot.model(), this);
        }
        return dataSet;
    }
}
