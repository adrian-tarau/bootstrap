package net.microfalx.bootstrap.help;

import com.vladsch.flexmark.html.*;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.html.MutableAttributes;
import net.microfalx.lang.FileUtils;
import net.microfalx.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Set;

import static net.microfalx.lang.StringUtils.removeStartSlash;

/**
 * A markdown extension to handle help related rendering.
 */
public class HelpExtension implements HtmlRenderer.HtmlRendererExtension {

    private static final String IMAGES_PATH = "images/";

    private final HelpService helpService;
    private final String path;

    HelpExtension(HelpService helpService, String path) {
        this.helpService = helpService;
        this.path = path;
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(HtmlRenderer.@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.linkResolverFactory(new LinkResolverImplFactory());
            htmlRendererBuilder.attributeProviderFactory(new AttributeProviderFactoryImpl());
        }
    }

    private String resolveImagePath(String path) {
        String parentPath = FileUtils.getParentPath(this.path);
        StringBuilder builder = new StringBuilder(helpService.getImagePath());
        if (StringUtils.isNotEmpty(parentPath)) builder.append('/').append(removeStartSlash(parentPath));
        builder.append('/').append(removeStartSlash(path));
        return builder.toString();
    }

    private String resolveArticlePath(String path) {
        StringBuilder builder = new StringBuilder(helpService.getArticlePath());
        builder.append('/').append(removeStartSlash(path));
        return builder.toString();
    }

    private ResolvedLink resolveImage(ResolvedLink link) {
        try {
            URI uri = URI.create(link.getUrl());
            String path = removeStartSlash(uri.getPath());
            if (uri.getScheme() == null && path != null && path.startsWith(IMAGES_PATH)) {
                return link.withUrl(resolveImagePath(path)).withStatus(LinkStatus.VALID);
            }
        } catch (Exception e) {
            // if we cannot parse then leave it as is
        }
        return link;
    }

    private ResolvedLink resolveLink(ResolvedLink link) {
        try {
            URI uri = URI.create(link.getUrl());
            String path = removeStartSlash(uri.getPath());
            if (uri.getScheme() == null) {
                return link.withUrl(resolveArticlePath(path)).withStatus(LinkStatus.VALID);
            }
        } catch (Exception e) {
            // if we cannot parse then leave it as is
        }
        return link.withTarget("_blank").withStatus(LinkStatus.VALID);
    }

    class AttributeProviderImpl implements AttributeProvider {

        @Override
        public void setAttributes(@NotNull Node node, @NotNull AttributablePart part, @NotNull MutableAttributes attributes) {
           // empty
        }
    }

    class AttributeProviderFactoryImpl implements AttributeProviderFactory {

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public @NotNull AttributeProvider apply(@NotNull LinkResolverContext context) {
            return new AttributeProviderImpl();
        }
    }

    class LinkResolverImpl implements LinkResolver {


        public LinkResolverImpl(LinkResolverBasicContext context) {
        }

        @Override
        public @NotNull ResolvedLink resolveLink(@NotNull Node node, @NotNull LinkResolverBasicContext context, @NotNull ResolvedLink link) {
            if (link.getLinkType() == LinkType.IMAGE) {
                return resolveImage(link);
            } else if (link.getLinkType() == LinkType.LINK) {
                return HelpExtension.this.resolveLink(link);
            } else {
                return link;
            }
        }
    }

    class LinkResolverImplFactory implements LinkResolverFactory {

        @Nullable
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Nullable
        @Override
        public Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @NotNull
        @Override
        public LinkResolver apply(@NotNull LinkResolverBasicContext context) {
            return new LinkResolverImpl(context);
        }
    }
}
