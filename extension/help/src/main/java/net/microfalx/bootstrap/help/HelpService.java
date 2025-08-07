package net.microfalx.bootstrap.help;

import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.media.tags.MediaTagsExtension;
import com.vladsch.flexmark.ext.resizable.image.ResizableImageExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.search.*;
import net.microfalx.lang.*;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.microfalx.bootstrap.help.HelpUtilities.*;
import static net.microfalx.bootstrap.search.Document.OWNER_FIELD;
import static net.microfalx.bootstrap.search.Document.TYPE_FIELD;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.removeEndSlash;

@Service
public class HelpService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpService.class);

    @Autowired
    private IndexService indexService;

    @Autowired private SearchService searchService;

    @Autowired(required = false) private HelpProperties properties = new HelpProperties();

    @Autowired private ThreadPool threadPool;

    private String imagePath = "/help/image";
    private String articlePath = "/help/article";

    private final Toc root = new Toc();
    final AtomicBoolean indexed = new AtomicBoolean(false);

    /**
     * Returns the root Table of Contents (ToC) entry.
     *
     * @return a non-null instance
     */
    public Toc getRoot() {
        return root;
    }

    /**
     * Returns the Table of Contents (ToC) entry for a given path.
     * <p>
     * The path is relative to the root of the help documentation.
     *
     * @param path the path to find
     * @return a non-null instance or null if not found
     */
    public Toc find(String path) {
        requireNonNull(path);
        if (UriUtils.isRoot(path)) {
            return root;
        } else {
            return root.findByPath(path);
        }
    }

    /**
     * Returns the Table of Contents (ToC) entry for a given path.
     *
     * @param path the to find
     * @return a non-null instance
     * @throws HelpNotFoundException if the help entry is not found
     */
    public Toc get(String path) {
        Toc toc = find(path);
        if (toc == null) {
            throw new HelpNotFoundException("Help entry not found for path '" + path + "'");
        }
        return toc;
    }

    /**
     * Returns whether the help service is ready.
     * <p>
     * The service is considered ready when the help content has been indexed.
     *
     * @return {@code true} if the service is ready, {@code false} otherwise
     */
    public boolean isReady() {
        return indexed.get();
    }

    /**
     * Returns the path where images can be resolved.
     *
     * @return a non-null instance
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Changes the path where images can be resolved.
     *
     * @param imagePath the path
     */
    public void setImagePath(String imagePath) {
        requireNotEmpty(imagePath);
        this.imagePath = removeEndSlash(imagePath);
        LOGGER.info("Image path changed to '{}'", imagePath);
    }

    /**
     * Returns the path where articles can be rendered.
     *
     * @return a non-null instance
     */
    public String getArticlePath() {
        return articlePath;
    }

    /**
     * Changes the path where images can be resolved.
     *
     * @param articlePath the path
     */
    public void setArticlePath(String articlePath) {
        requireNotEmpty(articlePath);
        this.articlePath = removeEndSlash(articlePath);
        LOGGER.info("Article path changed to '{}'", articlePath);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadTocs();
        indexHelp();
        threadPool.schedule(this::cacheContent, 5, TimeUnit.SECONDS);
    }

    /**
     * Searches through the help pages and returns a page of results.
     *
     * @param query the query to search for (if empty, returns an empty list)
     * @param page  the page number to return (0-based)
     * @param size  the size of the page (number of results per page)
     * @return a collection of Table of Contents (ToC) entries matching the query
     */
    public List<Toc> search(String query, int page, int size) {
        if (StringUtils.isEmpty(query)) return Collections.emptyList();
        query = SearchUtils.normalizeQuery(query, true);
        LOGGER.info("Search help for '{}' at page {} with size {}", query, page, size);
        String finalQuery = "(" + OWNER_FIELD + ": " + DOCUMENT_OWNER + " AND " + TYPE_FIELD + ": " + DOCUMENT_TYPE
                + ") AND (" + query + ")";
        SearchQuery searchQuery = new SearchQuery(finalQuery)
                .setTimeless(true).setStart(page * size).setLimit(size);
        SearchResult result = searchService.search(searchQuery);
        LOGGER.info("Found {} TOC entries matching the query", result.getTotalHits());
        List<Toc> tocs = new ArrayList<>();
        for (Document document : result.getDocuments()) {
            Attribute path = document.get(PATH_FIELD);
            if (path == null) continue;
            Toc toc = find((String) path.getValue());
            if (toc != null) {
                tocs.add(toc);
            } else {
                LOGGER.debug("A TOC entry found in index for path '{}' does not exist anymore", path);
                indexService.remove(document.getId());
            }
        }
        return tocs;
    }

    /**
     * Transforms the help content.
     * <p>
     * The transformation includes parsing the Markdown content and applying content changes to
     * match the target rendering options.
     *
     * @param options the rendering options to apply
     * @return a Resource containing the transformed content
     * @throws IOException if an I/O error occurs
     */
    public Resource transformAll(RenderingOptions options) throws IOException {
        requireNonNull(options);
        options = options.copy().heading(true).build();
        Resource resource = Resource.file(getHelpCacheFile(options, MARKDOWN_EXTENSION));
        if (!resource.exists()) {
            Writer writer = resource.getWriter();
            transform(root, writer, options, root.getPath());
            writer.close();
        }
        return resource;
    }

    /**
     * Transforms the content of a Table of Contents (ToC) entry.
     * <p>
     * The transformation includes parsing the Markdown content and applying content changes to
     * match the target rendering options.
     *
     * @param toc     the Table of Contents (ToC) entry to transform
     * @param options the rendering options to apply
     * @return a Resource containing the transformed content
     * @throws IOException if an I/O error occurs
     */
    public Resource transform(Toc toc, RenderingOptions options) throws IOException {
        requireNonNull(toc);
        requireNonNull(options);
        ContentTransformer transformer = new ContentTransformer(toc.getContent()).setToc(toc).setOptions(options);
        return transformer.execute();
    }

    /**
     * Renders a document.
     * <p>
     * The path of the document is extracted from the annotated element.
     *
     * @param element the annotated element
     * @throws IOException if an I/O error occurs
     * @return the rendered content as a Resource
     * @see Help
     */
    public Resource render(AnnotatedElement element, RenderingOptions options) throws IOException {
        Help helpAnnot = AnnotationUtils.getAnnotation(element, Help.class);
        if (helpAnnot == null) {
            throw new HelpException("The annotated element '" + element + "' does not have an @Help annotation");
        }
        return render(get(helpAnnot.value()), options);
    }

    /**
     * Renders the content of the whole help as a single page.
     *
     * @param options the options for rendering
     * @throws IOException if an I/O error occurs
     * @return the rendered content as a Resource
     */
    public Resource renderAll(RenderingOptions options) throws IOException {
        requireNonNull(options);
        options = options.copy().heading(true).build();
        Resource resource = Resource.file(getHelpCacheFile(options, HTML_EXTENSION));
        if (!resource.exists()) {
            Writer writer = resource.getWriter();
            render(root, writer, options, root.getPath());
            writer.close();
        }
        return resource;
    }

    /**
     * Renders the Table of Contents (ToC) as for the whole help.
     *
     * @param options the rendering options
     * @return the TOC as HTML
     */
    public Resource renderToc(RenderingOptions options) {
        TocRenderer renderer = new TocRenderer(root, options);
        return Resource.text(renderer.render());
    }

    /**
     * Renders the content of the resource.
     *
     * @param resource the resource to render
     * @throws IOException if an I/O error occurs
     */
    public String render(Resource resource) throws IOException {
        StringWriter writer = new StringWriter();
        render(resource, writer);
        return writer.toString();
    }

    /**
     * Renders a document.
     *
     * @param toc   the toc to render
     * @param options the options for rendering
     * @throws IOException if an I/O error occurs
     */
    public Resource render(Toc toc, RenderingOptions options) throws IOException {
        requireNonNull(toc);
        LOGGER.info("Render TOC at '{}'", toc.getPath());
        Resource resource = Resource.file(getTocCacheFile(toc, options, HTML_EXTENSION));
        if (!resource.exists()) {
            // resolve the Markdown file
            Writer writer = resource.getWriter();
            render(toc.getContent(), writer, options, toc);
            writer.close();
        }
        return resource;
    }

    /**
     * Renders a document.
     *
     * @param resource the resource to render
     * @param writer   the writer to write the rendered content to
     * @throws IOException if an I/O error occurs
     */
    public void render(Resource resource, Writer writer) throws IOException {
        render(resource, writer, RenderingOptions.DEFAULT);
    }

    /**
     * Renders a document.
     *
     * @param resource the resource to render
     * @param writer   the writer to write the rendered content to
     * @param options  the options for rendering
     * @throws IOException if an I/O error occurs
     */
    public void render(Resource resource, Writer writer, RenderingOptions options) throws IOException {
        render(resource, writer, options, null);
    }

    private void render(Resource resource, Writer writer, RenderingOptions renderingOptions, Toc toc) throws IOException {
        requireNonNull(resource);
        requireNonNull(writer);
        if (!resource.exists()) {
            throw new HelpNotFoundException("The markdown resource '" + resource.toURI() + "' does not exist");
        }
        LOGGER.debug("Render markdown content '{}'", resource.toURI());
        // initialize the HTML renderer
        MutableDataSet markdownOptions = new MutableDataSet();
        registerExtensions(markdownOptions, resource, renderingOptions);
        updateOptions(markdownOptions);
        Parser parser = Parser.builder(markdownOptions).build();
        HtmlRenderer renderer = HtmlRenderer.builder(markdownOptions).build();
        // transforms the content
        ContentTransformer transformer = new ContentTransformer(resource).setOptions(renderingOptions);
        if (toc != null) transformer.setToc(toc);
        resource = transformer.execute();
        // render the file
        Node document = parser.parse(resource.loadAsString());
        addAnchor(toc, writer);
        renderer.render(document, writer);
    }

    private void registerExtensions(MutableDataSet markdownOptions, Resource resource, RenderingOptions renderingOptions) {
        String path = FileUtils.removeFileExtension(resource.getPath());
        markdownOptions.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(),
                AdmonitionExtension.create(), MediaTagsExtension.create(), FootnoteExtension.create(),
                TaskListExtension.create(),ResizableImageExtension.create(), GitLabExtension.create(),
                TypographicExtension.create(), AnchorLinkExtension.create(), DefinitionExtension.create(),
                EmojiExtension.create(), TocExtension.create(),
                new HelpExtension(this, path)));
    }

    private void updateOptions(MutableDataSet options) {
        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, HtmlRenderer.HARD_BREAK.getDefaultValue());
        options.set(TablesExtension.CLASS_NAME, "table table-striped");
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor");
        options.set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true);
        // Optional TOC options
        options.set(TocExtension.LEVELS, 255); // All levels
        options.set(TocExtension.TITLE, "Table of Contents");
    }

    private void loadTocs() {
        HelpLoader loader = new HelpLoader();
        loader.load(root);
    }

    private void indexHelp() {
        HelpIndexer indexer = new HelpIndexer(this, indexService, root);
        threadPool.schedule(indexer, 2, TimeUnit.SECONDS);
    }

    private void addAnchor(Toc toc, Writer writer) throws IOException {
        if (toc == null) return;
        writer.append("<a id='").append(HelpUtilities.getAnchorId(toc.getPath())).append("'></a>\n");
    }

    private void render(Toc toc, Writer writer, RenderingOptions options, String path) throws IOException {
        writer.append("\n\n");
        if (toc.getContent().exists()) {
            render(toc.getContent(), writer, options, toc);
        } else if (!toc.isRoot()) {
            writer.append("> No content available for '").append(path).append("'\n");
        }
        int nextLevel = Math.max(3, options.getLevel() + 1);
        options = options.copy().level(nextLevel).build();
        for (Toc child : toc.getChildren()) {
            render(child, writer, options, toc.getPath());
        }
    }

    private void transform(Toc toc, Writer writer, RenderingOptions options, String path) throws IOException {
        writer.append("\n\n");
        if (toc.getContent().exists()) {
            Resource content = transform(toc, options);
            writer.append(content.loadAsString());
        } else if (!toc.isRoot()) {
            writer.append("> No content available for '").append(path).append("'\n");
        }
        int nextLevel = Math.max(3, options.getLevel() + 1);
        options = options.copy().level(nextLevel).build();
        for (Toc child : toc.getChildren()) {
            transform(child, writer, options, toc.getPath());
        }
    }

    private void removeOldFiles() {
        FileUtils.remove(getCacheDirectory(), FileUtils.ALL_FILES, 1);
    }

    private void cacheContent() {
        removeOldFiles();
        LOGGER.info("Cache help content");
        try {
            renderAll(RenderingOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.warn("Failed to cache help's HTML content", e);
        }
        try {
            transformAll(RenderingOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.warn("Failed to cache help's Markdown content", e);
        }
        try {
            transformAll(RenderingOptions.builder().navigation(true).build());
        } catch (IOException e) {
            LOGGER.warn("Failed to cache help's Markdown with navigation content", e);
        }
    }

    private String getCacheFileName(String id, RenderingOptions options, String extension) {
        return StringUtils.toIdentifier(id) + "_" + options.getHash() + "." + extension;
    }

    private File getTocCacheFile(Toc toc, RenderingOptions options, String extension) {
        return new File(getCacheDirectory(), getCacheFileName(toc.getPath(), options, extension));
    }

    private File getHelpCacheFile(RenderingOptions options, String extension) {
        return new File(getCacheDirectory(), getCacheFileName("help", options, extension));
    }

    private File getCacheDirectory() {
        return JvmUtils.getCacheDirectory("help");
    }
}
