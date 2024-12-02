package net.microfalx.bootstrap.content;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.microfalx.bootstrap.content.impl.JsonDetector;
import net.microfalx.lang.ClassUtils;
import net.microfalx.resource.*;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static net.microfalx.bootstrap.content.ContentUtils.removeRedundantNewLines;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A service which uses Apache Tika to extract content.
 */
@Service
public class ContentService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentService.class);

    private static final String ID_PREFIX = Long.toString(System.currentTimeMillis(), Character.MAX_RADIX);
    private static final AtomicLong ID_SUFFIX = new AtomicLong(1);

    private final Collection<ContentResolver> resolvers = new CopyOnWriteArrayList<>();
    private final Collection<ContentPersister> persisters = new CopyOnWriteArrayList<>();
    private final Collection<ContentRenderer> renderers = new CopyOnWriteArrayList<>();
    private final Cache<String, Content> cachedContents = CacheBuilder.newBuilder().maximumSize(5000)
            .expireAfterAccess(Duration.ofMinutes(5)).build();
    private volatile Detector detector = new CompositeDetector();
    private final List<Detector> detectors = new CopyOnWriteArrayList<>();
    private final Map<String, Fragment> fragments = new ConcurrentHashMap<>();
    private TikaConfig tikaConfig;

    /**
     * Registers a custom text detector.
     * <p>
     * Since Apache Tika uses magic words to detect mime types, after everything fails, it tries to detect
     * if the content is text. If the mime type is "text/plain", a special list of text detectors are executed
     * after Apache Tika to try to detect if it is a special type of text.
     * <p>
     * These detectors are invoked only if the result of the detection is {@link org.apache.tika.mime.MimeTypes#PLAIN_TEXT}
     *
     * @param detector the text detector
     */
    public void registerTextDetector(Detector detector) {
        requireNonNull(detector);
        LOGGER.info("Register text detector '" + ClassUtils.getName(detector));
        detectors.add(detector);
        this.detector = new CompositeDetector(tikaConfig.getMediaTypeRegistry(), detectors);
    }

    /**
     * Detects the language of the text
     *
     * @param resource the content of the document
     * @return the language
     * @throws IOException if an I/O exception
     */
    public String detectLanguage(Resource resource) throws IOException {
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        LanguageResult result = detector.detect(resource.loadAsString());
        return result.getLanguage();
    }

    /**
     * Returns the mime type for the given document.
     *
     * @param resource the content of the document
     * @return the mime type
     * @throws IOException if an I/O exception occurs
     */
    public MediaType detectMimeType(Resource resource) throws IOException {
        return detectMimeType(resource, new Metadata());
    }

    /**
     * Returns the mime type for the given document.
     * <p>
     * The client can help with detection by providing metadata using {@link TikaCoreProperties} or {@link Metadata} attributes.
     *
     * @param resource the content of the document
     * @return the mime type
     * @throws IOException if an I/O exception occurs
     */
    public MediaType detectMimeType(Resource resource, Metadata metadata) throws IOException {
        requireNonNull(resource);
        requireNonNull(metadata);
        metadata = updateMetadata(metadata, resource);
        InputStream inputStream = resource.getInputStream();
        return detector.detect(inputStream, metadata);
    }

    /**
     * Registers a content for later reference.
     * <p>
     * The content is expired after a configurable amount of time if it is not accessed.
     *
     * @param content the content
     * @return the identifier associated with the content
     * @see #getContent(String)
     */
    public String registerContent(Content content) {
        requireNonNull(content);
        String id = ID_PREFIX + Long.toString(ID_SUFFIX.incrementAndGet(), Character.MAX_RADIX);
        cachedContents.put(id, content);
        return id;
    }

    /**
     * Retrieves a content previously registered with {@link #registerContent(Content)}.
     *
     * @param id the content identifier
     * @return the content
     * @throws ContentNotFoundException if the content does not exist
     * @see #registerContent(Content)
     */
    public Content getContent(String id) {
        requireNonNull(id);
        Content content = cachedContents.getIfPresent(id);
        if (content == null) {
            throw new ContentNotFoundException("A content with identifier '" + id + "' is not registered");
        }
        return content;
    }

    /**
     * Parses a document using an available parser.
     *
     * @param resource       the content of the document
     * @param contentHandler the content handler
     * @throws IOException if an I/O exception occurs
     */
    public void parse(Resource resource, ContentHandler contentHandler) throws IOException {
        parse(resource, contentHandler, new Metadata());
    }

    /**
     * Parses a document using an available parser.
     *
     * @param resource       the content of the document
     * @param contentHandler the content handler
     * @param metadata       the metadata collector
     * @throws IOException if an I/O exception occurs
     */
    public void parse(Resource resource, ContentHandler contentHandler, Metadata metadata) throws IOException {
        requireNonNull(resource);
        requireNonNull(contentHandler);
        doParse(resource, contentHandler, metadata);
    }

    /**
     * Extracts the plain text from a document using an available parser.
     *
     * @param resource the content of the document
     * @throws IOException if an I/O exception occurs
     */
    public Content extract(Resource resource) throws IOException {
        return extract(resource, false);
    }

    /**
     * Extracts the plain text from a document using an available parser.
     *
     * @param resource the content of the document
     * @throws IOException if an I/O exception occurs
     */
    public Content extract(Resource resource, boolean extractAttributes) throws IOException {
        return extract(resource, extractAttributes, new Metadata());
    }

    /**
     * Extracts the plain text from a document using an available parser.
     *
     * @param resource the content of the document
     * @throws IOException if an I/O exception occurs
     */
    public Content extract(Resource resource, boolean extractAttributes, Metadata metadata) throws IOException {
        requireNonNull(resource);
        requireNonNull(metadata);
        if (MimeType.APPLICATION_OCTET_STREAM.equals(resource.getMimeType())) {
            BinaryContentExtractor extractor = new BinaryContentExtractor(resource);
            return Content.create(MemoryResource.create(extractor.execute()));
        } else {
            ContentExtractor handler = new ContentExtractor();
            handler.setExtractAttributes(extractAttributes);
            doParse(resource, handler, metadata);
            Resource extractedContent = MemoryResource.create(removeRedundantNewLines(handler.toString()));
            Content content = Content.create(extractedContent);
            if (extractAttributes) content = content.withAttributes(handler.getAttributes());
            return content;
        }
    }

    /**
     * Resolves a content based on its identifier and URI.
     *
     * @param locator the content locator
     * @return the resource, null if it cannot resolve the content
     */
    public Content resolve(ContentLocator locator) {
        requireNotEmpty(locator);
        for (ContentResolver resolver : resolvers) {
            if (resolver.supports(locator)) {
                Throwable throwable = null;
                Content content = null;
                try {
                    content = resolver.resolve(locator);
                } catch (IOException e) {
                    throwable = e;
                }
                if (content == null) throw new ContentException("Content cannot be resolved by listener '" +
                        ClassUtils.getName(resolver) + ", locator '" + locator + "'", throwable);
                return content;
            }
        }
        throw new ContentNotFoundException("Content cannot be resolved by any listener, locator '" + locator + "'");
    }

    /**
     * Renders the content fully or partially based on the page information in
     * a format that's suitable for viewing the content.
     *
     * @param content the content
     * @return the resource with
     */
    public Resource view(Content content) {
        requireNotEmpty(content);
        for (ContentRenderer renderer : renderers) {
            if (renderer.supports(content)) {
                Throwable throwable = null;
                Resource resource = null;
                try {
                    resource = renderer.view(content);
                    resource = renderer.prettyPrint(resource);
                } catch (IOException e) {
                    throwable = e;
                }
                if (resource == null) throw new ContentException("Content cannot be viewed by listener '" +
                        ClassUtils.getName(renderer) + ", locator '" + content.getLocator() + "'", throwable);
                return resource;
            }
        }
        return content.getResource();
    }

    /**
     * Renders the content fully or partially based on the page information in
     * a format that's suitable for changing the content.
     *
     * @param content the content
     * @return the resource with
     */
    public Resource edit(Content content) {
        requireNotEmpty(content);
        for (ContentRenderer renderer : renderers) {
            if (renderer.supports(content)) {
                Throwable throwable = null;
                Resource resource = null;
                try {
                    resource = renderer.edit(content);
                    resource = renderer.prettyPrint(resource);
                } catch (IOException e) {
                    throwable = e;
                }
                if (resource == null) throw new ContentException("Content cannot be edited by listener '" +
                        ClassUtils.getName(renderer) + ", locator '" + content.getLocator() + "'", throwable);
                return resource;
            }
        }
        return content.getResource();
    }

    /**
     * Intercepts the content.
     *
     * @param content the content
     * @return the resource with
     */
    public Content intercept(Content content) {
        requireNotEmpty(content);
        for (ContentResolver resolver : resolvers) {
            if (resolver.supports(content.getLocator())) {
                Throwable throwable = null;
                try {
                    content = resolver.intercept(content);
                } catch (IOException e) {
                    throwable = e;
                }
                if (content == null) throw new ContentException("Content cannot be left NULL by listener '" +
                        ClassUtils.getName(resolver) + ", locator '" + content.getLocator() + "'", throwable);
                return content;
            }
        }
        return content;
    }

    /**
     * Resolves a content based on its locator and prepares the content to be edited.
     *
     * @param resource the resource
     * @return the resource
     * @throws ContentException if the content cannot be resolved or presented
     */
    public Resource prettyPrint(Resource resource) {
        requireNotEmpty(resource);
        Content content = Content.create(resource);
        for (ContentRenderer renderer : renderers) {
            if (renderer.supports(content)) {
                Throwable throwable = null;
                try {
                    resource = renderer.prettyPrint(resource);
                } catch (IOException e) {
                    throwable = e;
                }
                if (resource == null) throw new ContentException("Content cannot be edited by listener '" +
                        ClassUtils.getName(renderer) + ", locator '" + content.getLocator() + "'", throwable);
                return resource;
            }
        }
        throw new ContentException("No listener can pretty-print content referenced by locator '" + content.getLocator() + "'");
    }

    /**
     * Updates the content.
     *
     * @param content the resource holding the new content
     */
    public void update(Content content) throws IOException {
        requireNotEmpty(content);
        requireNonNull(content);
        requireNonNull(content);
        for (ContentPersister persister : persisters) {
            if (persister.supports(content.getLocator())) {
                persister.persist(content);
                return;
            }
        }
        throw new ContentException("No listener can update content referenced by locator '" + content.getLocator() + "'");
    }

    /**
     * Returns a fragment by its identifier.
     *
     * @param id the identifier
     * @return the fragment
     */
    public Fragment getFragment(String id) {
        Fragment fragment = fragments.get(toIdentifier(id));
        if (fragment == null) {
            throw new ContentNotFoundException("A fragment with identifier '" + id + "' not found");
        }
        return fragment;
    }

    /**
     * Registers a new fragment.
     *
     * @param fragment the fragment
     */
    public void registerFragment(Fragment fragment) {
        requireNonNull(fragment);
        String id = toIdentifier(fragment.getId());
        Fragment prevFragment = fragments.putIfAbsent(id, fragment);
        if (prevFragment != null) {
            throw new ContentException("A fragment with identifier '" + id + "' already exists");
        }
    }

    /**
     * Returns all registered fragments.
     *
     * @return a non-null instance
     */
    public Collection<Fragment> getFragments() {
        return Collections.unmodifiableCollection(fragments.values());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverListeners();
        discoverFragments();
        setupTika();
        registerTextDetectors();
        registerSuperTypes();
        registerResourceMimeTypeDetector();
    }

    private void setupTika() {
        tikaConfig = TikaConfig.getDefaultConfig();
        detectors.add(tikaConfig.getDetector());
        LOGGER.info("Loaded Apache Tika, mime types {}, detector {}, parser {}",
                tikaConfig.getMediaTypeRegistry().getTypes().size(), ClassUtils.getName(tikaConfig.getDetector()),
                ClassUtils.getName(tikaConfig.getParser()));
    }

    private void registerTextDetectors() {
        registerTextDetector(new JsonDetector());
    }

    private void registerSuperTypes() {
        tikaConfig.getMediaTypeRegistry().addSuperType(MediaType.parse(MimeType.APPLICATION_SQL.getValue()), MediaType.TEXT_PLAIN);
    }

    private void registerResourceMimeTypeDetector() {
        for (MimeTypeResolver mimeTypeResolver : ResourceFactory.getMimeTypeResolvers()) {
            if (mimeTypeResolver instanceof ContentMimeTypeResolver resolver) resolver.contentService = this;
        }
    }

    private void doParse(Resource resource, ContentHandler contentHandler, Metadata metadata) throws IOException {
        AutoDetectParser parser = new AutoDetectParser(tikaConfig);
        parser.setDetector(detector);
        metadata = updateMetadata(metadata, resource);
        ParseContext context = new ParseContext();
        try {
            try (InputStream stream = resource.getInputStream()) {
                parser.parse(stream, contentHandler, updateMetadata(metadata, resource), context);
            }
        } catch (SAXException e) {
            throw new ContentParsingException("Failed to parse document '" + resource.toURI() + "' due to syntax errors", e);
        } catch (TikaException e) {
            throw new ContentParsingException("Failed to parse document '" + resource.toURI() + "'", e);
        }
    }

    private Metadata updateMetadata(Metadata metadata, Resource resource) {
        //metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, resource.getFileName());
        return metadata;
    }

    private void discoverListeners() {
        LOGGER.debug("Discover content resolvers:");
        Collection<ContentResolver> discoveredContentResolvers = ClassUtils.resolveProviderInstances(ContentResolver.class);
        for (ContentResolver contentListener : discoveredContentResolvers) {
            LOGGER.debug(" - " + ClassUtils.getName(contentListener));
            resolvers.add(contentListener);
        }
        LOGGER.info("Discovered " + resolvers.size() + " content resolvers");
        LOGGER.debug("Discover content persisters:");
        Collection<ContentPersister> discoveredContentPersisters = ClassUtils.resolveProviderInstances(ContentPersister.class);
        for (ContentPersister contentPersister : discoveredContentPersisters) {
            LOGGER.debug(" - {}", ClassUtils.getName(contentPersister));
            persisters.add(contentPersister);
        }
        LOGGER.info("Discovered {} content persisters", persisters.size());
        LOGGER.debug("Discover content renderers:");
        Collection<ContentRenderer> discoveredContentRenderers = ClassUtils.resolveProviderInstances(ContentRenderer.class);
        for (ContentRenderer contentRenderer : discoveredContentRenderers) {
            LOGGER.debug(" - {}", ClassUtils.getName(contentRenderer));
            renderers.add(contentRenderer);
        }
        LOGGER.info("Discovered {} content renderers", renderers.size());
    }

    private void discoverFragments() {
        FragmentLoader loader = new FragmentLoader(this);
        loader.load();
    }

}
