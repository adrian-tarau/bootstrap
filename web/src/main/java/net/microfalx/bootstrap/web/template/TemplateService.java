package net.microfalx.bootstrap.web.template;

import jakarta.annotation.PostConstruct;
import net.microfalx.bootstrap.content.ContentService;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.help.HelpService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.chart.ChartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service("WebTemplateService")
public class TemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateService.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private HelpService helpService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private ChartService chartService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    private void initialize() {
        templateEngine.addDialect(new SpringDataDialect());
        templateEngine.addDialect(new ApplicationDialect(applicationContext));
        templateEngine.addDialect(new AssetDialect(applicationContext));
        templateEngine.addDialect(new ExpressionsDialect(applicationContext));
        templateEngine.addDialect(new ComponentDialect(applicationContext));
        templateEngine.addDialect(new ChartDialect(applicationContext));
    }

}
