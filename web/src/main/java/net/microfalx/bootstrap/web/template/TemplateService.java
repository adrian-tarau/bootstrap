package net.microfalx.bootstrap.web.template;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class TemplateService {

    private Logger LOGGER = LoggerFactory.getLogger(TemplateService.class);

    @Autowired
    private SpringTemplateEngine templateEngine;

    @PostConstruct
    private void initialize() {
        templateEngine.addDialect(new SpringDataDialect());
    }


}