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
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.search.*;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import net.microfalx.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import static net.microfalx.lang.StringUtils.removeStartSlash;

@Service
public class HelpService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpService.class);

    public static final String RESOURCE_PATH = "/help";

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
        return root.findByPath(path);
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
        this.articlePath = articlePath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadTocs();
        indexHelp();
    }

    /**
     * Returns the class path to an image.
     *
     * @param path the image path
     * @return a non-null instance
     */
    public String resolveImage(String path) {
        requireNonNull(path);
        return RESOURCE_PATH + "/" + removeStartSlash(path);
    }

    /**
     * Returns the class path to a content file.
     *
     * @param path the content path
     * @return a non-null instance
     */
    public String resolveContent(String path) {
        requireNonNull(path);
        return RESOURCE_PATH + "/" + removeStartSlash(removeEndSlash(path)) + ".md";
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
     * Renders a document.
     * <p>
     * The path of the document is extracted from the annotated element.
     *
     * @param element the annotated element
     * @param writer the writer to write the rendered content to
     * @throws IOException if an I/O error occurs
     * @see Help
     */
    public void render(AnnotatedElement element, Writer writer) throws IOException {
        Help helpAnnot = AnnotationUtils.getAnnotation(element, Help.class);
        if (helpAnnot == null) {
            throw new HelpException("The annotated element '" + element + "' does not have an @Help annotation");
        }
        render(helpAnnot.value(), writer);
    }

    /**
     * Renders the content of the whole help as a single page.
     *
     * @throws IOException if an I/O error occurs
     */
    public Resource renderAll() throws IOException {
        Resource temporary = Resource.temporary("help", "md");
        Writer writer = temporary.getWriter();
        render(root, writer, root.getPath());
        writer.close();
        return temporary;
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
     * @param path   the path in the file system, without extension
     * @param writer the writer to write the rendered content to
     * @throws IOException if an I/O error occurs
     */
    public void render(String path, Writer writer) throws IOException {
        requireNonNull(path);
        LOGGER.info("Render help at '{}'", path);
        // resolve the Markdown file
        Resource resource = HelpUtilities.resolve(path);
        render(resource, writer);
    }

    /**
     * Renders a document.
     *
     * @param resource the resource to render
     * @param writer the writer to write the rendered content to
     * @throws IOException if an I/O error occurs
     */
    public void render(Resource resource, Writer writer) throws IOException {
        render(resource, writer, resource.getPath());
    }

    private void render(Resource resource, Writer writer, String path) throws IOException {
        requireNonNull(resource);
        LOGGER.debug("Render help content '{}'", resource.toURI());

        // initialize the HTML renderer
        MutableDataSet options = new MutableDataSet();
        registerExtensions(options, resource);
        updateOptions(options);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        if (!resource.exists()) {
            throw new HelpNotFoundException("A help entry at path '" + resource.toURI() + "' does not exist");
        }

        // render the file
        Node document = parser.parse(resource.loadAsString());
        renderer.render(document, writer);
    }


    private void registerExtensions(MutableDataSet options, Resource resource) {
        String path = FileUtils.removeFileExtension(resource.getPath());
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(),
                AdmonitionExtension.create(), MediaTagsExtension.create(), FootnoteExtension.create(),
                TaskListExtension.create(),ResizableImageExtension.create(), GitLabExtension.create(),
                TypographicExtension.create(), AnchorLinkExtension.create(), DefinitionExtension.create(),
                EmojiExtension.create(),
                new HelpExtension(this, path)));
    }

    private void updateOptions(MutableDataSet options) {
        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, HtmlRenderer.HARD_BREAK.getDefaultValue());
        options.set(TablesExtension.CLASS_NAME, "table table-striped");
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "anchor");
        options.set(AnchorLinkExtension.ANCHORLINKS_SET_NAME, true);
    }

    private void loadTocs() {
        HelpLoader loader = new HelpLoader();
        loader.load(root);
    }

    private void indexHelp() {
        HelpIndexer indexer = new HelpIndexer(this, indexService, root);
        threadPool.schedule(indexer, 2, TimeUnit.SECONDS);
    }

    private void render(Toc toc, Writer writer, String path) throws IOException {
        writer.append("\n\n");
        if (toc.getContent().exists()) {
            render(toc.getContent(), writer);
        } else {
            writer.append("> No content available for '").append(toc.getPath()).append("'\n");
        }
        for (Toc child : toc.getChildren()) {
            render(child, writer, toc.getPath());
        }
    }

}
