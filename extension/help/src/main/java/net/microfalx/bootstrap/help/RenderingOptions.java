package net.microfalx.bootstrap.help;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Hashing;

/**
 * Options for rendering TOCs.
 */
@Builder
@Getter
@ToString
public class RenderingOptions {

    /**
     * Default rendering options: no headings, no navigation, and no level transformation.
     */
    public static final RenderingOptions DEFAULT = RenderingOptions.builder().build();

    /**
     * If true, a headings (level 1) will be added to a TOC.
     */
    private boolean heading;

    /**
     * If true, a navigation heading (level 2, at the beginning of the document.
     */
    private boolean navigation;

    /**
     * The level of the TOC transformation to render. A value of 0 means no transformation,
     */
    private int level;

    /**
     * Creates a new builder for these options.
     *
     * @return a new builder instance
     */
    public RenderingOptionsBuilder copy() {
        return RenderingOptions.builder()
                .heading(heading)
                .navigation(navigation)
                .level(level);
    }

    /**
     * Returns a hash of the options.
     *
     * @return a non-null string
     */
    public String getHash() {
        return Long.toString(Math.abs(Hashing.create().update(heading).update(navigation).update(level)
                .asLong()), Character.MAX_RADIX);
    }
}
