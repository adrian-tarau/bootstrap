package net.microfalx.bootstrap.logger;

import net.microfalx.resource.Resource;
import net.microfalx.resource.ResourceException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.XmlUtils.*;

/**
 * Loads appenders and logger settings.
 */
public class LoggerLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerLoader.class);
    private final Collection<Appender> appenders = new ArrayList<>();

    Collection<Appender> getAppenders() {
        return appenders;
    }

    void load() {
        LOGGER.debug("Discover logger appenders from descriptors");
        Collection<URL> descriptors = null;
        try {
            descriptors = getDescriptors();
            for (URL descriptor : descriptors) {
                try {
                    load(Resource.url(descriptor));
                } catch (Exception e) {
                    LOGGER.atError().setCause(e)
                            .log("Failed to load loggers from descriptor: {}", descriptor.toExternalForm());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover descriptors", e);
        }
        LOGGER.debug("Discovered {} logger appenders from descriptors", appenders.size());
    }

    void load(Resource resource) throws IOException {
        if (!resource.exists()) {
            throw new ResourceException("The resource " + resource.getPath() + " does not exist");
        }
        LOGGER.debug("Load loggers from {}", resource.toURI());
        Document document = loadDocument(resource.getInputStream());
        Element rootElement = document.getRootElement();
        loadAppenders(rootElement);
    }

    private void loadAppenders(Element root) {
        List<Element> appenderElements = root.elements("appender");
        for (Element appenderElement : appenderElements) {
            Appender.Builder appenderBuilder = Appender.builder(getRequiredAttribute(appenderElement, "id"));
            String name = getAttribute(appenderElement, "name");
            if (isNotEmpty(name)) appenderBuilder.name(name);
            String fileName = getAttribute(appenderElement, "file-name");
            if (isNotEmpty(fileName)) appenderBuilder.fileName(fileName);
            loadAppenderElements(appenderElement, appenderBuilder);
            appenders.add(appenderBuilder.build());
        }
    }

    private void loadAppenderElements(Element root, Appender.Builder appenderBuilder) {
        List<Element> includeElements = root.elements("include");
        for (Element includeElement : includeElements) {
            appenderBuilder.included(getElementText(includeElement, true));
        }
        List<Element> excludeElements = root.elements("exclude");
        for (Element excludeElement : excludeElements) {
            appenderBuilder.included(getElementText(excludeElement, true));
        }
    }

    static Collection<URL> getDescriptors() throws IOException {
        Collection<URL> urls = new ArrayList<>();
        Enumeration<URL> resources = LoggerLoader.class.getClassLoader().getResources("logger.xml");
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }
}
