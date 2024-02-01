package net.microfalx.bootstrap.help;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.search.IndexService;
import net.microfalx.lang.AnnotationUtils;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

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

    @Autowired
    private HelpProperties helpProperties;

    private String imagePath = "/help/image";
    private String articlePath = "/help/article";

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
        // empty for now
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
     * Renders a document.
     * <p>
     * The path of the document is extracted from the annotated element.
     *
     * @param element the annotated element
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
     * Renders a document.
     *
     * @param path the path in the file system, without extension
     * @throws IOException if an I/O error occurs
     */
    public void render(String path, Writer writer) throws IOException {
        requireNonNull(path);
        LOGGER.info("Render help at '" + path + "'");

        // initialize the HTML renderer
        MutableDataSet options = new MutableDataSet();
        registerExtensions(options, path);
        updateOptions(options);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // resolve the Markdown file
        path = resolveContent(path);
        Resource resource = ClassPathResource.file(path);
        if (!resource.exists()) throw new HelpNotFoundException("A help entry at path '" + path + "' does not exist");

        // render the file
        Node document = parser.parse(resource.loadAsString());
        renderer.render(document, writer);
    }

    private void registerExtensions(MutableDataSet options, String path) {
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(),
                new HelpExtension(this, path)));
    }

    private void updateOptions(MutableDataSet options) {
        // uncomment to convert soft-breaks to hard breaks
        options.set(HtmlRenderer.SOFT_BREAK, HtmlRenderer.HARD_BREAK.getDefaultValue());
        options.set(TablesExtension.CLASS_NAME, "table table-striped");
    }
}
