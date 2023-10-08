package net.microfalx.bootstrap.web.dashboard;

import net.microfalx.bootstrap.dataset.DataSetException;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.annotation.Name;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Base class for all dashboards.
 */
@Controller
public abstract class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("")
    public String render(Model model) {
        getDashboard(model);
        return "dashboard/index";
    }

    private Dashboard getDashboard(Model model) {
        net.microfalx.bootstrap.web.dashboard.annotation.Dashboard dashboardAnnot = getDashboardAnnotation();
        Dashboard dashboard = dashboardService.getDashboard(dashboardAnnot.value());
        dashboard.setTitle(getTitle());
        model.addAttribute("dashboard", dashboard);
        return dashboard;
    }

    private String getTitle(){
        Name nameAnnot = AnnotationUtils.getAnnotation(this, Name.class);
        return nameAnnot != null ? nameAnnot.value() : null;
    }

    private net.microfalx.bootstrap.web.dashboard.annotation.Dashboard getDashboardAnnotation() {
        net.microfalx.bootstrap.web.dashboard.annotation.Dashboard dashboardAnnot = AnnotationUtils.getAnnotation(this, net.microfalx.bootstrap.web.dashboard.annotation.Dashboard.class);
        if (dashboardAnnot == null) {
            throw new DataSetException("A @Dashboard annotation could not be located for controller " + ClassUtils.getName(this));
        }
        return dashboardAnnot;
    }
}
