package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.XmlUtils.*;

/**
 * Loads navigation from <code>asset.xml</code> descriptors.
 */
class NavigationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetBundleLoader.class);

    private ApplicationService applicationService;

    NavigationLoader(ApplicationService applicationService) {
        requireNonNull(applicationService);
        this.applicationService = applicationService;
    }

    void load() {
        LOGGER.info("Discover navigation from web descriptors");
        Collection<URL> webDescriptors = null;
        try {
            webDescriptors = ApplicationUtils.getNavigationDescriptors();
            for (URL webDescriptor : webDescriptors) {
                try {
                    loadNavigations(webDescriptor);
                } catch (Exception e) {
                    LOGGER.error("Failed to load web resources from navigation descriptors: " + webDescriptor.toExternalForm(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover web descriptors", e);
        }
        if (webDescriptors != null) {
            for (URL webDescriptor : webDescriptors) {
                LOGGER.info(webDescriptor.toExternalForm());
            }
        }
    }

    private void loadNavigations(URL webDescriptor) throws IOException {
        LOGGER.info("Load resources from " + webDescriptor.toExternalForm());
        Document document = loadDocument(webDescriptor.openStream());
        Element rootElement = document.getRootElement();
        List<Element> navigationElements = rootElement.elements("navigation");
        for (Element navigationElement : navigationElements) {
            String id = getRequiredAttribute(navigationElement, "id");
            Navigation navigation;
            try {
                navigation = applicationService.getNavigation(id);
            } catch (Exception e) {
                navigation = new Navigation(id);
                applicationService.registerNavigation(navigation);
            }
            loadLinks(navigationElement, navigation, null);
        }
    }

    private void loadLinks(Element root, Navigation navigation, Link parent) {
        List<Element> linkElements = root.elements("link");
        for (Element linkElement : linkElements) {
            Link link = new Link(getRequiredAttribute(linkElement, "name"));
            String target = getAttribute(linkElement, "target");
            if (target != null) link.setTarget(target);
            String roles = getAttribute(linkElement, "roles", StringUtils.EMPTY_STRING);
            Arrays.asList(StringUtils.split(roles, ",")).forEach(link::addRole);
            link.setOrder(getAttribute(linkElement, "order", -1));
            link.setIcon(getAttribute(linkElement, "icon"));
            if (parent != null) {
                parent.add(link);
            } else {
                navigation.add(link);
            }
            loadLinks(linkElement, navigation, link);
        }
    }
}
