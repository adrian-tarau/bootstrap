package net.microfalx.bootstrap.jdbc.migration;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.XmlUtils;
import net.microfalx.resource.Resource;
import net.microfalx.resource.UrlResource;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;
import static net.microfalx.lang.XmlUtils.*;

/**
 * Loads schema definitions from <code>schema.xml</code>.
 */
@Slf4j
public class DefinitionLoader {

    private final Map<String, Definition> definitions = new HashMap<>();
    private final Map<String, Module> modules = new HashMap<>();
    private Module currentModule;
    private int loadOrder = 100;

    public Collection<Module> getModules() {
        return unmodifiableCollection(modules.values());
    }

    /**
     * Returns the module for the given id.
     *
     * @param id the module identifier
     * @return the module
     */
    public Module getModule(String id) {
        requireNotEmpty(id);
        Module module = modules.get(toIdentifier(id));
        if (module == null) throw new IllegalArgumentException("No module found for identifier " + id);
        return module;
    }

    /**
     * Returns the definition for the given id.
     *
     * @param id the definition identifier
     * @return the definition
     */
    public Definition getDefinition(String id) {
        requireNotEmpty(id);
        Definition definition = definitions.get(toIdentifier(id));
        if (definition == null) throw new IllegalArgumentException("No definition found for identifier " + id);
        return definition;
    }

    /**
     * Returns loaded definitions.
     *
     * @return a non-modifiable collection of definitions
     */
    public Collection<Definition> getDefinitions() {
        List<Definition> sortedDefinitions = new ArrayList<>(definitions.values());
        sortedDefinitions.sort(Comparator.comparingInt(Definition::getOrder));
        return unmodifiableCollection(sortedDefinitions);
    }

    /**
     * Loads schema definitions from <code>schema.xml</code> files found in the classpath.
     */
    public void load() {
        LOGGER.debug("Discover schema definitions from database descriptors");
        Collection<URL> descriptors;
        try {
            descriptors = getDescriptors();
            for (URL descriptor : descriptors) {
                load(UrlResource.create(descriptor));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover schema descriptors", e);
        }
        reorderModules();
        LOGGER.debug("Discovered {} schema descriptors, in {} modules", definitions.size(), modules.size());
    }

    /**
     * Loads schema definitions from the given resource.
     *
     * @param resource the resource
     */
    public void load(Resource resource) {
        try {
            doLoad(resource);
        } catch (Exception e) {
            LOGGER.atError().setCause(e).log("Failed to load schema from descriptor {}", resource);
        }
    }

    private void doLoad(Resource resource) throws IOException {
        ArgumentUtils.requireNonNull(resource);
        LOGGER.debug("Load resources from {}", resource);
        Document document = loadDocument(resource.getReader());
        Element rootElement = document.getRootElement();
        loadModule(rootElement);
        loadDefinitions(rootElement);
        reorderModules();
    }

    private void loadModule(Element root) {
        currentModule = new Module(getRequiredAttribute(root, "id"), getRequiredAttribute(root, "name"));
        currentModule.order = getAttribute(root, "order", -1);
        if (currentModule.order < 0) currentModule.order = loadOrder;
        loadOrder += 1;
        modules.put(currentModule.getId(), currentModule);
        List<Element> dependsOnElements = root.elements("depends-on");
        for (Element dependsOnElement : dependsOnElements) {
            currentModule.dependsOn.add(XmlUtils.getElementText(dependsOnElement));
        }
    }

    private void loadDefinitions(Element root) {
        List<Element> definitionElements = root.elements("definition");
        int order = 0;
        for (Element definitionElement : definitionElements) {
            Definition definition = new Definition(currentModule, getRequiredAttribute(definitionElement, "name"),
                    getRequiredAttribute(definitionElement, "path"));
            definition.order = order++;
            loadDefinition(definitionElement, definition);
            loadMigrations(definitionElement, definition);
            definitions.put(definition.getId(), definition);
        }
    }

    private void loadDefinition(Element definitionElement, Definition definition) {
        loadTables(definitionElement, definition);
    }

    private void loadTables(Element definitionElement, Definition definition) {
        List<Element> tableElements = definitionElement.elements("table");
        for (Element tableElement : tableElements) {
            definition.tables.add(XmlUtils.getElementText(tableElement));
        }
    }

    private void loadMigrations(Element definitionElement, Definition definition) {
        List<Element> migrationElements = definitionElement.elements("migration");
        for (Element migrationElement : migrationElements) {
            Migration migration = new Migration(definition, getRequiredAttribute(migrationElement, "path"));
            migration.condition = getRequiredAttribute(migrationElement, "condition");
            definition.addMigration(migration);
        }
    }

    private void reorderModules() {
        int changes;
        do {
            changes = 0;
            for (Module module : modules.values()) {
                changes += reorderModule(module);
            }
        } while (changes != 0);
    }


    private int reorderModule(Module module) {
        int changes = 0;
        int maxOrder = module.getOrder();
        for (String dependsOn : module.getDependsOn()) {
            Module parentModule = getModule(dependsOn);
            if (maxOrder <= parentModule.getOrder()) {
                changes++;
                maxOrder = Math.max(maxOrder, parentModule.getOrder() + 1);
            }
        }
        module.order = maxOrder;
        return changes;
    }

    private static Collection<URL> getDescriptors() throws IOException {
        Collection<URL> urls = new ArrayList<>();
        Enumeration<URL> resources = DefinitionLoader.class.getClassLoader().getResources("schema.xml");
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }

}
