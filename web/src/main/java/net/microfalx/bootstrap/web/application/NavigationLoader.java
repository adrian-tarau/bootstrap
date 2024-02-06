package net.microfalx.bootstrap.web.application;

import net.microfalx.bootstrap.web.component.*;
import net.microfalx.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.XmlUtils.*;

/**
 * Loads navigation from <code>asset.xml</code> descriptors.
 */
final class NavigationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationLoader.class);

    private ApplicationService applicationService;
    private Queue<Pending> pending = new ArrayDeque<>();

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
        processPending();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processPending() {
        int iterations = 50;
        while (iterations-- > 0 && !pending.isEmpty()) {
            Pending pending = this.pending.poll();
            Menu navigation;
            try {
                navigation = applicationService.getNavigation(pending.navigation);
            } catch (Exception e) {
                LOGGER.error("Missing navigation with identifier '" + pending.navigation + " while processing pending items");
                continue;
            }
            Container<?> parent = (Container<?>) navigation.find(pending.parent, true);
            if (parent == null) {
                this.pending.offer(pending);
            } else {
                LOGGER.debug("Add child '" + pending.child.getId() + "' to parent '" + pending.parent + "'");
                parent.add((Component) pending.child);
            }
        }
        if (!pending.isEmpty()) {
            LOGGER.error(pending.size() + " navigation entries could not be registered due to missing parents:");
            for (Pending pending : this.pending) {
                Menu navigation;
                try {
                    navigation = applicationService.getNavigation(pending.navigation);
                } catch (Exception e) {
                    continue;
                }
                LOGGER.error("A parent ({}) could not be resolved in navigation '{}' for entry '{}'", pending.parent, navigation.getId(), pending.child.getId());
            }
        }
    }

    private boolean hasPendingContainers() {
        for (Pending pending : this.pending) {
            if (pending.child instanceof Container<?>) return true;
        }
        return false;
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
            loadChildren(navigationElement, navigation, navigation);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void loadChildren(Element root, Menu navigation, Container<?> parent) {
        List<Element> elements = root.elements();
        for (Element element : elements) {
            String parentComponent = getAttribute(element, "parent");
            Component component = switch (element.getName()) {
                case "item" -> loadItem(element, parent);
                case "spacer" -> loadSpacer(element, parent);
                case "menu" -> loadMenu(element, parent);
                default -> null;
            };
            if (parentComponent != null) {
                LOGGER.debug("Parent '" + parentComponent + "' could not be located in navigation '" + navigation.getId() + ", queue");
                pending.offer(new Pending(navigation.getId(), parentComponent, component));
            } else if (component != null) {
                parent.add(component);
            }
            if (component instanceof Container) loadChildren(element, navigation, (Container<?>) component);
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

    private Component<?> loadSpacer(Element element, Container<?> parent) {
        Spacer spacer = new Spacer();
        spacer.setPosition(getAttribute(element, "position", -1));
        return spacer;
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

    static class Pending {

        private final String navigation;
        private final String parent;
        private final Component<?> child;

        Pending(String navigation, String parent, Component<?> child) {
            requireNonNull(navigation);
            requireNonNull(parent);
            requireNonNull(child);
            this.navigation = navigation;
            this.parent = parent;
            this.child = child;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Pending.class.getSimpleName() + "[", "]")
                    .add("navigation='" + navigation + "'")
                    .add("parent='" + parent + "'")
                    .add("child=" + child)
                    .toString();
        }
    }
}
