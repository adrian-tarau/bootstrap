package net.microfalx.bootstrap.help;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.isNotEmpty;
import static net.microfalx.lang.XmlUtils.*;

@Slf4j
class HelpLoader {

    private final static int PENDING_RESOLVE_LIMIT = 10;

    private Toc root;

    int tocCount = 0;
    private final Map<String, Collection<Toc>> pendingTocs = new HashMap<>();

    void load(Toc root) {
        requireNonNull(root);
        this.root = root;
        LOGGER.debug("Discover TOC entries from help descriptors");
        Collection<URL> helpDescriptors;
        try {
            helpDescriptors = getDescriptors();
            for (URL webDescriptor : helpDescriptors) {
                try {
                    loadResources(webDescriptor);
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to load web resources from asset descriptors: {}",
                            webDescriptor.toExternalForm());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover help entries", e);
        }
        resolvePending();
        LOGGER.info("Discovered {} help TOCs from help descriptors", tocCount);
    }

    private void loadResources(URL helpDescriptor) throws IOException {
        LOGGER.debug("Load help resources from {}", helpDescriptor.toExternalForm());
        Document document = loadDocument(helpDescriptor.openStream());
        Element rootElement = document.getRootElement();
        loadTocs(rootElement, root);
    }

    private void loadTocs(Element root, Toc parent) {
        List<Element> rocElements = root.elements("toc");
        for (Element tocElement : rocElements) {
            Toc newParent = loadToc(tocElement, parent);
            loadTocs(tocElement, newParent);
        }
    }

    private Toc loadToc(Element tocElement, Toc parent) {
        Toc toc = new Toc(getRequiredAttribute(tocElement, "id"), getRequiredAttribute(tocElement, "name"));
        toc.order = getAttribute(tocElement, "order", -1);
        String path = getAttribute(tocElement, "path");
        if (isNotEmpty(path)) {
            Toc locatedParent = root.findByPath(path);
            if (locatedParent != null) {
                locatedParent.addChild(toc);
            } else {
                pendingTocs.computeIfAbsent(path, s -> new ArrayList<>())
                        .add(toc);
            }
        } else {
            parent.addChild(toc);
        }
        tocCount++;
        return toc;
    }

    private Collection<URL> getDescriptors() throws IOException {
        Collection<URL> urls = new ArrayList<>();
        Enumeration<URL> resources = HelpLoader.class.getClassLoader().getResources("help.xml");
        while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
        }
        return urls;
    }

    private void resolvePending() {
        if (pendingTocs.isEmpty()) return;
        LOGGER.debug("Resolving pending TOCs");
        for (int i = 0; i < PENDING_RESOLVE_LIMIT; i++) {
            if (pendingTocs.isEmpty()) break;
            resolvePendingOnce();
        }
        for (Map.Entry<String, Collection<Toc>> entry : pendingTocs.entrySet()) {
            LOGGER.warn("Failed to resolve pending TOC entries for path '{}'. {} entries not resolved.",
                    entry.getKey(), entry.getValue().size());
        }
    }

    private void resolvePendingOnce() {
        Set<String> resolvedPaths = new HashSet<>();
        for (Map.Entry<String, Collection<Toc>> entry : pendingTocs.entrySet()) {
            Toc parent = root.findByPath(entry.getKey());
            if (parent != null) {
                resolvedPaths.add(entry.getKey());
                for (Toc toc : entry.getValue()) {
                    parent.addChild(toc);
                }
            }
        }
        for (String resolvedPath : resolvedPaths) {
            pendingTocs.remove(resolvedPath);
        }
    }
}
