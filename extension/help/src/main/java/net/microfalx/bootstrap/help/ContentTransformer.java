package net.microfalx.bootstrap.help;

import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.resource.Resource;

import java.io.BufferedReader;
import java.io.IOException;

import static java.lang.Character.isUpperCase;
import static java.util.stream.Collectors.joining;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which transforms the markdown content.
 */
class ContentTransformer {

    private final Resource resource;
    private final StringBuilder builder = new StringBuilder();
    private Toc toc;
    private RenderingOptions options = RenderingOptions.DEFAULT;
    private String levelPrefix = "";

    ContentTransformer(Resource resource) {
        requireNonNull(resource);
        this.toc = null;
        this.resource = resource;
    }

    ContentTransformer setToc(Toc toc) {
        requireNonNull(toc);
        this.toc = toc;
        return this;
    }

    ContentTransformer setOptions(RenderingOptions options) {
        requireNonNull(options);
        this.options = options;
        initLevelPrefix();
        return this;
    }

    Resource execute() throws IOException {
        builder.setLength(0);
        appendPrefix();
        doExecute();
        appendSuffix();
        return Resource.text(builder.toString()).withMimeType(resource.getMimeType())
                .withName(resource.getName());
    }

    private void doExecute() throws IOException {
        BufferedReader reader = new BufferedReader(resource.getReader());
        reader.lines().forEach(this::processLine);
    }

    private void processLine(String line) {
        if (isHeading(line)) increaseLevel();
        builder.append(line).append("\n");
    }

    private void increaseLevel() {
        builder.append(levelPrefix);
    }

    private void appendPrefix() {
        if (toc == null || toc.isRoot()) return;
        if (options.isHeading()) {
            increaseLevel();
            builder.append("# ").append(toc.getName()).append("\n\n");
        }
        if (options.isNavigation()) {
            increaseLevel();
            builder.append("## Navigation\nWhere to find this page in the app:\n")
                    .append("- Go to: ").append(getBreadcrumbs()).append("\n")
                    .append("\n\n");
        }
    }

    private void appendSuffix() {

    }

    private boolean isHeading(String line) {
        if (line.isEmpty()) return false;
        int index = 0;
        while (index < line.length()) {
            if (line.charAt(index) != '#') break;
            index++;
        }
        if (index < 2) return false;
        while (index < line.length()) {
            if (line.charAt(index) != ' ') break;
            index++;
        }
        return isUpperCase(line.charAt(index));
    }

    private void initLevelPrefix() {
        this.levelPrefix = "#".repeat(Math.max(0, options.getLevel()));
    }

    private String getBreadcrumbs() {
        return toc.getParents().stream().map(NamedIdentityAware::getName)
                .collect(joining(" > "));
    }


}
