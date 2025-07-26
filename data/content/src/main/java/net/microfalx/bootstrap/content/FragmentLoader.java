package net.microfalx.bootstrap.content;

import net.microfalx.lang.EnumUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.ResourceUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.XmlUtils.*;

/**
 * Loads fragments from resources.
 */
class FragmentLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentLoader.class);

    private final ContentService contentService;
    private int snippets;
    private int examples;
    private int invalid;

    FragmentLoader(ContentService contentService) {
        this.contentService = contentService;
    }

    void load() {
        LOGGER.debug("Discover fragments from resources");
        Collection<URL> contentDescriptors;
        try {
            contentDescriptors = ContentUtils.getContentDescriptors();
            for (URL contentDescriptor : contentDescriptors) {
                try {
                    loadFragments(contentDescriptor);
                } catch (Exception e) {
                    LOGGER.atError().setCause(e).log("Failed to load web resources from navigation descriptors: {}",
                            contentDescriptor.toExternalForm());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to discover web descriptors", e);
        }
        LOGGER.info("Discovered {} examples and {} snippets from content descriptors, invalid {}", examples, snippets, invalid);
    }

    private void loadFragments(URL contentDescriptor) throws IOException {
        LOGGER.debug("Load fragments from {}", contentDescriptor.toExternalForm());
        Document document = loadDocument(contentDescriptor.openStream());
        Element rootElement = document.getRootElement();
        Element editor = rootElement.element("editor");
        if (editor == null) return;
        List<Element> fragmentElements = editor.elements("fragment");
        for (Element fragmentElement : fragmentElements) {
            String id = getRequiredAttribute(fragmentElement, "id");
            Fragment.Builder builder = new Fragment.Builder(id);
            Fragment fragment = loadFragment(builder, fragmentElement);
            if (fragment != null) {
                contentService.registerFragment(fragment);
            } else {
                invalid++;
            }
        }
    }

    private Fragment loadFragment(Fragment.Builder builder, Element fragmentElement) {
        builder.group(getRequiredAttribute(fragmentElement, "group"))
                .path(getRequiredAttribute(fragmentElement, "resource"))
                .abbreviation(getAttribute(fragmentElement, "description", (String) null))
                .name(getRequiredAttribute(fragmentElement, "name"))
                .description(getAttribute(fragmentElement, "description", (String) null));
        String typeAsText = getRequiredAttribute(fragmentElement, "type");
        try {
            Fragment.Type type = EnumUtils.fromName(Fragment.Type.class, typeAsText);
            builder.type(type);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unknown fragment type '{}' for fragment '{}'", typeAsText, builder.id());
            return null;
        }
        String languageAsText = getAttribute(fragmentElement, "language", (String) null);
        if (StringUtils.isNotEmpty(languageAsText)) {
            try {
                Fragment.Language type = EnumUtils.fromName(Fragment.Language.class, languageAsText);
                builder.language(type);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Unknown fragment language '{}' for fragment '{}'", typeAsText, builder.id());
                return null;
            }
        }
        Fragment fragment = builder.build();
        if (!ResourceUtils.exists(fragment.getResource())) {
            LOGGER.error("Fragment resource '{}' for fragment '{}' does not exist", fragment.getPath(), builder.id());
            return null;
        }
        switch (fragment.getType()) {
            case SNIPPET -> snippets++;
            case EXAMPLE -> examples++;
        }
        return fragment;
    }
}
