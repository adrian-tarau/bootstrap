package net.microfalx.bootstrap.web.application;

import net.microfalx.bootstrap.web.component.*;
import net.microfalx.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
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
    }

    private void loadNavigations(URL webDescriptor) throws IOException {
        LOGGER.info("Load resources from " + webDescriptor.toExternalForm());
        Document document = loadDocument(webDescriptor.openStream());
        Element rootElement = document.getRootElement();
        List<Element> menuElements = rootElement.elements("menu");
        for (Element navigationElement : menuElements) {
            String id = getRequiredAttribute(navigationElement, "id");
            Menu navigation;
            try {
                navigation = applicationService.getNavigation(id);
            } catch (Exception e) {
                navigation = new Menu().setId(id);
                applicationService.registerNavigation(navigation);
            }
            loadChildren(navigationElement, navigation);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadChildren(Element root, Container<?> parent) {
        List<Element> elements = root.elements();
        for (Element element : elements) {
            Component component = switch (element.getName()) {
                case "item" -> loadItem(element, parent);
                case "menu" -> loadMenu(element, parent);
                default -> null;
            };
            if (component != null) {
                parent.add(component);
            }
            if (component instanceof Container) loadChildren(element, (Container<?>) component);
        }
    }

    private Component<?> loadMenu(Element element, Container<?> parent) {
        String id = getRequiredAttribute(element, "id");
        Menu menu = parent.find(id);
        if (menu == null) menu = new Menu().setId(id);
        updateActionable(element, menu);
        return menu;
    }

    private Component<?> loadItem(Element element, Container<?> parent) {
        String id = getRequiredAttribute(element, "id");
        Item item = new Item().setId(id);
        updateActionable(element, item);
        return item;
    }

    private void updateActionable(Element element, Actionable<?> actionable) {
        actionable.setAction(getAttribute(element, "action"));
        actionable.setToken(getAttribute(element, "token"));
        actionable.setText(getAttribute(element, "text"));
        String roles = getAttribute(element, "roles", StringUtils.EMPTY_STRING);
        actionable.addRoles(StringUtils.split(roles, ","));
        ((Component<?>) actionable).setPosition(getAttribute(element, "position", -1));
        actionable.setIcon(getAttribute(element, "icon"));
    }
}
