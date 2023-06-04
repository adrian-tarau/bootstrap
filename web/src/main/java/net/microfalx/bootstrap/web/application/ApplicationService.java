package net.microfalx.bootstrap.web.application;

import jakarta.annotation.PostConstruct;
import net.microfalx.bootstrap.web.container.WebContainerService;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.TextUtils;
import net.microfalx.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A service which provides metadata for a web application.
 */
@Service
public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private WebContainerService webContainerService;

    private AssetBundleManager assetBundleManager = new AssetBundleManager(this);
    private Map<String, Navigation> navigations = new ConcurrentHashMap<>();
    private final Application application = new Application();

    /**
     * Returns the application description.
     *
     * @return a non-null instance
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Registers a navigation.
     *
     * @param navigation the navigation
     */
    public void registerNavigation(Navigation navigation) {
        requireNonNull(navigation);
        if (navigations.containsKey(navigation.getId())) {
            throw new ApplicationException("A navigation with identifier '" + navigation.getId() + " is already registered");
        }
        navigations.put(navigation.getId(), navigation);
    }

    /**
     * Returns the navigation with a given identifier.
     *
     * @param id the navigation identifier
     * @return the navigation
     * @throws ApplicationException if the navigation is not registered
     */
    public Navigation getNavigation(String id) {
        requireNonNull(id);
        Navigation navigation = navigations.get(id.toLowerCase());
        if (navigation == null) {
            throw new ApplicationException("A navigation with identifier '" + id + "' is not registered");
        }
        return navigation;
    }

    /**
     * Returns all registered navigations.
     *
     * @return a non-null instance
     */
    public Collection<Navigation> getNavigations() {
        return Collections.unmodifiableCollection(navigations.values());
    }

    /**
     * Registers a new asset bundle.
     *
     * @param assetBundle the asset bundle
     */
    public void registerAssetBundle(AssetBundle assetBundle) {
        assetBundleManager.registerAssetBundle(assetBundle);
    }

    /**
     * Returns a previously registered asset bundle.
     *
     * @param id the asset bundle identifier
     * @return the resource group with the newest version
     */
    public AssetBundle getAssetBundle(String id) {
        return assetBundleManager.getAssetBundle(id);
    }

    /**
     * Returns the content of an asset bundle.
     *
     * @param id   the asset bundle identifier
     * @param type the type of the asset
     * @return the resource with the bundle content
     */
    public Resource getAssetBundleContent(String id, Asset.Type type) throws IOException {
        return assetBundleManager.getAssetBundleContent(id, type);
    }

    /**
     * Returns a previously registered collection of asset bundle.
     *
     * @param ids the asset bundle identifiers
     * @return the resource group with the newest version
     */
    public Collection<AssetBundle> getAssetBundles(String... ids) {
        Collection<AssetBundle> assetBundles = new ArrayList<>();
        for (String id : ids) {
            assetBundles.add(getAssetBundle(id));
        }
        return assetBundles;
    }

    /**
     * Returns the HTML tag to include this asset bundle in a web page.
     *
     * @param assetBundle the asset
     * @param type        the asset type to referenced
     * @return the HTML tag
     */
    public String getAssetBundleTag(AssetBundle assetBundle, Asset.Type type) {
        requireNonNull(assetBundle);
        requireNonNull(type);

        StringBuilder builder = new StringBuilder();
        String path;
        switch (type) {
            case JAVA_SCRIPT -> {
                builder.append("<script type=\"text/javascript\" src=\"");
                path = "script";
            }
            case STYLE_SHEET -> {
                builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                path = "stylesheet";
            }
            default -> throw new IllegalStateException("Unhandled asset type: " + type);
        }
        path = webContainerService.getPath(path);
        path += "/" + assetBundle.getId();
        builder.append(path);
        if (!StringUtils.isNotEmpty(assetBundle.getVersion())) {
            builder.append("&version=").append(assetBundle.getVersion());
        }
        builder.append("\">");
        switch (type) {
            case JAVA_SCRIPT:
                builder.append("</script>");
                break;
            case STYLE_SHEET:
                //builder.append("</link>"); No end tag for link!
                break;
            default:
                throw new IllegalStateException("Unhandled asset type: " + type);
        }
        return builder.toString();
    }


    /**
     * Returns the HTML tags to include the stylesheets provided by the given bundles.
     *
     * @param assetBundles a list of asset bundles
     * @return the HTML tags
     */
    public String getStylesheets(String... assetBundles) {
        return getStylesheets(0, assetBundles);
    }

    /**
     * Returns the HTML tags to include the stylesheets provided by the given bundles.
     *
     * @param assetBundles a list of asset bundles
     * @param indent       how many spaces to insert in front of each line
     * @return the HTML tags
     */
    public String getStylesheets(int indent, String... assetBundles) {
        return getAssetBundleTags(Asset.Type.STYLE_SHEET, indent, getAssetBundles(assetBundles));
    }

    /**
     * Returns the HTML tags to include the scripts provided by the given bundles.
     *
     * @param assetBundles a list of asset bundles
     * @return the HTML tags
     */
    public String getScripts(String... assetBundles) {
        return getScripts(0, assetBundles);
    }

    /**
     * Returns the HTML tags to include the scripts provided by the given bundles.
     *
     * @param assetBundles a list of asset bundles
     * @param indent       how many spaces to insert in front of each line
     * @return the HTML tags
     */
    public String getScripts(int indent, String... assetBundles) {
        return getAssetBundleTags(Asset.Type.JAVA_SCRIPT, indent, getAssetBundles(assetBundles));
    }

    /**
     * Returns the HTML tags to include the asset bundles.
     *
     * @param type         the asset type to return
     * @param assetBundles a list of asset bundles
     * @param indent       how many spaces to insert in front of each line
     * @return the HTML tags
     */
    private String getAssetBundleTags(Asset.Type type, int indent, Collection<AssetBundle> assetBundles) {
        requireNonNull(assetBundles);

        StringBuilder builder = new StringBuilder();
        Iterator<AssetBundle> assetBundleIterator = assetBundles.iterator();
        while (assetBundleIterator.hasNext()) {
            AssetBundle assetBundle = assetBundleIterator.next();
            String assetBundleTag = getAssetBundleTag(assetBundle, type);
            builder.append(assetBundleTag);
            if (assetBundleIterator.hasNext()) {
                builder.append('\n');
            }
        }
        return TextUtils.insertSpaces(builder.toString(), indent);
    }


    @PostConstruct
    private void initialize() {
        initApplication();
        initAssets();
    }

    private void initApplication() {
        application.name = applicationProperties.getName();
        application.description = applicationProperties.getDescription();
    }

    private void initAssets() {
        assetBundleManager.load();
    }

    private String getImageBasePath() {
        return webContainerService.getPath("image");
    }

    private String getFontBasePath() {
        return webContainerService.getPath("font");
    }


}
