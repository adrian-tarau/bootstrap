package net.microfalx.bootstrap.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.lang.EnumUtils;
import net.microfalx.resource.Resource;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static net.microfalx.bootstrap.configuration.ConfigurationUtils.ROOT_METADATA_ID;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.XmlUtils.*;

@Slf4j
class ConfigurationLoader {

    @Getter
    private int groupCount;
    @Getter
    private int itemCount;

    private final Stack<Metadata> metadataStack = new Stack<>();
    private final Map<String, Metadata> metadataMap = new HashMap<>();

    ConfigurationLoader() {
        Metadata root = new Metadata(null, ROOT_METADATA_ID, ROOT_METADATA_ID);
        metadataStack.push(root);
        metadataMap.put(root.getId(), root);
    }

    Map<String, Metadata> getMetadata() {
        return unmodifiableMap(metadataMap);
    }

    void load() {
        LOGGER.debug("Discover configuration from descriptors");
        Collection<URL> configurationDescriptors;
        try {
            configurationDescriptors = ConfigurationUtils.getDescriptors();
            for (URL configurationDescriptor : configurationDescriptors) {
                try {
                    loadResources(configurationDescriptor);
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to load web resources from configuration description {}",
                            configurationDescriptor.toExternalForm());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover configuration descriptors", e);
        }
        LOGGER.info("Discovered {} groups and {} items from descriptors", groupCount, itemCount);
    }

    void load(Resource resource) throws IOException {
        Document document = loadDocument(resource.getInputStream());
        Element rootElement = document.getRootElement();
        loadGroups(rootElement);
    }

    private void loadResources(URL url) throws IOException {
        LOGGER.debug("Load resources from {}", url.toExternalForm());
        load(Resource.url(url));
    }

    private void loadGroups(Element root) {
        List<Element> groupElements = root.elements("group");
        for (Element groupElement : groupElements) {
            loadGroup(groupElement);
            loadGroups(groupElement);
            loadItems(groupElement);
            metadataStack.pop();
        }
    }

    private void loadGroup(Element groupElement) {
        Metadata metadata = new Metadata(getParent(), getRequiredAttribute(groupElement, "key"),
                getRequiredAttribute(groupElement, "name"));
        loadOrder(metadata, groupElement);
        loadDescription(metadata, groupElement);
        metadata.section = getAttribute(groupElement, "section", (String) null);
        register(metadata);
        metadataStack.push(metadata);
        groupCount++;
    }

    private void loadItems(Element groupElements) {
        List<Element> itemElements = groupElements.elements("item");
        for (Element itemElement : itemElements) {
            Metadata metadata = new Metadata(getParent(), getRequiredAttribute(itemElement, "key"),
                    getRequiredAttribute(itemElement, "name"));
            loadOrder(metadata, itemElement);
            loadDescription(metadata, itemElement);
            metadata.dataType = EnumUtils.fromName(Metadata.DataType.class,
                    getAttribute(itemElement, "data-type", (String) null), Metadata.DataType.STRING);
            loadRange(metadata, itemElement);
            loadComponent(metadata, itemElement);
            register(metadata);
            itemCount++;
        }
    }

    private void loadRange(Metadata metadata, Element element) {
        String minimumString = getAttribute(element, "minimum", (String) null);
        String maximumString = getAttribute(element, "maximum", (String) null);
        switch (metadata.dataType) {
            case INTEGER -> {
                if (isNotEmpty(minimumString)) metadata.minimum = Long.parseLong(minimumString);
                if (isNotEmpty(maximumString)) metadata.maximum = Long.parseLong(maximumString);
            }
            case NUMBER -> {
                if (isNotEmpty(minimumString)) metadata.minimum = Double.parseDouble(minimumString);
                if (isNotEmpty(maximumString)) metadata.maximum = Double.parseDouble(maximumString);
            }
        }
    }

    private void loadOrder(Metadata metadata, Element element) {
        metadata.order = getAttribute(element, "order", Integer.MIN_VALUE);
        if (metadata.order == Integer.MIN_VALUE) metadata.order = getParent().getChildren().size();
    }

    private void loadDescription(Metadata metadata, Element element) {
        metadata.description = getAttribute(element, "description", (String) null);
    }

    private void loadComponent(Metadata metadata, Element element) {
        metadata.lineCount = getAttribute(element, "line-count", 0);
        metadata.multiline = metadata.lineCount > 1;
    }

    private void register(Metadata metadata) {
        metadataMap.put(metadata.getId(), metadata);
        getParent().add(metadata);
    }

    private Metadata getParent() {
        return metadataStack.peek();
    }

}
