package net.microfalx.bootstrap.support.report;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isEmpty;

/**
 * Renders a Thymeleaf templates.
 */
@Slf4j
public class Template implements Nameable {

    public static final String APPLICATION_VARIABLE = "application";

    private final TemplateEngine templateEngine;
    private final String name;
    private final Map<String, Object> variables = new HashMap<>();
    private String selector;

    Template(TemplateEngine templateEngine, String name) {
        requireNonNull(templateEngine);
        requireNonNull(name);
        this.templateEngine = templateEngine;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Changes the selector used to render a fragment of the template.
     *
     * @param selector the selector
     * @return a non-null instance
     */
    public Template setSelector(String selector) {
        this.selector = selector;
        return this;
    }

    /**
     * Adds a new variable to the template context.
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     * @return self
     */
    public Template addVariable(String name, Object value) {
        requireNonNull(name);
        Object previous = variables.put(name, value);
        if (previous != null && !previous.equals(value)) {
            LOGGER.warn("Overriding existing template variable '{}' ({} -> {})", name, previous, value);
        }
        return this;
    }

    /**
     * Returns whether a variable with the given name exists.
     *
     * @param name the name of the variable
     * @return {@code true} if the variable exists, {@code false} otherwise
     */
    public boolean hasVariable(String name) {
        requireNonNull(name);
        return variables.containsKey(name);
    }

    /**
     * Renders a template.
     *
     * @throws IOException if an I/O error occurs
     */
    public void render(Resource resource) throws IOException {
        requireNonNull(resource);
        Context context = initContext();
        TemplateSpec template = initTemplate();
        try (Writer writer = resource.getWriter()) {
            templateEngine.process(template, context, writer);
        }
    }

    private Context initContext() {
        Context context = new Context();
        ReportHelper helper = new ReportHelper();
        context.setVariable("helper", helper);
        context.setVariables(variables);
        return context;
    }

    private TemplateSpec initTemplate() {
        if (isEmpty(selector)) {
            return new TemplateSpec(name, TemplateMode.HTML);
        } else {
            return new TemplateSpec(name, Set.of(selector), TemplateMode.HTML, Collections.emptyMap());
        }
    }


}
