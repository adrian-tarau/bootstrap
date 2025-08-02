package net.microfalx.bootstrap.help;


import static net.microfalx.bootstrap.help.HelpUtilities.getAnchorId;

class TocRenderer {

    private final Toc toc;
    private final RenderingOptions options;

    private final StringBuilder builder = new StringBuilder();

    public TocRenderer(Toc toc, RenderingOptions options) {
        this.toc = toc;
        this.options = options;
    }

    String render() {
        builder.setLength(0);
        renderToc(toc, 0);
        return builder.toString();
    }

    private void renderToc(Toc toc, int level) {
        if (!toc.isRoot()) {
            builder.append("\n").append(getIndent(level)).append("<li>")
                    .append("<a onclick=\"")
                    .append("Help.select('").append(getAnchorId(toc.getPath())).append("')\">")
                    .append("<span>").append(toc.getNumbering()).append("</span>")
                    .append(toc.getName()).append("</a>");
        }
        if (toc.hasChildren()) {
            level++;
            builder.append("\n").append(getIndent(level)).append("<ol>");
        }
        for (Toc child : toc.getChildren()) {
            renderToc(child, level + 1);
        }
        if (toc.hasChildren()) {
            builder.append("\n").append(getIndent(level)).append("</ol>\n");
            level--;
            builder.append(getIndent(level));
        }
        if (!toc.isRoot()) builder.append("</li>");
    }


    private String getIndent(int level) {
        return " ".repeat(3 * Math.max(0, level));
    }

}
