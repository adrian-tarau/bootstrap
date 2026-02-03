package net.microfalx.bootstrap.web.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetBundleLoaderTest {

    @Mock private ApplicationService applicationService;
    @Mock private ApplicationContext applicationContext;

    @Spy private Application application = new Application();
    @Spy AssetProperties properties = new AssetProperties();

    @InjectMocks
    private AssetBundleManager assetBundleManager;

    private AssetBundleLoader loader;

    @BeforeEach
     void setup() {
        application.version = "1.0.0";
        when(applicationService.getApplication()).thenReturn(application);
        loader = new AssetBundleLoader(assetBundleManager);
        assetBundleManager.initialize(applicationContext, properties);
        doLoad();
    }

    @Test
    void load() {
        assertEquals(19, assetBundleManager.getAssetBundleCount());
    }

    @Test
    void loadJQuery() throws IOException {
        AssetBundle bundle = assetBundleManager.getAssetBundle("jquery");
        assertEquals(5, bundle.getAssets().size());
        assertTrue(bundle.has(Asset.Type.JAVA_SCRIPT));
        assertFalse(bundle.has(Asset.Type.STYLE_SHEET));
        assertFalse(bundle.has(Asset.Type.FONT));
        assertFalse(bundle.has(Asset.Type.IMAGE));
        Iterator<Asset> iterator = bundle.getAssets().iterator();

        Asset asset = iterator.next();
        assertEquals("jquery_js", asset.getId());
        assertEquals("jquery.js", asset.getName());
        assertEquals("jquery.js", asset.getPath());
        assertEquals(0, asset.getOrder());
        assertEquals("text/javascript", asset.getContentType());
        assertTrue(asset.getResource().exists());
    }

    @Test
    void loadBootstrap() throws IOException {
        AssetBundle bundle = assetBundleManager.getAssetBundle("bootstrap");
        assertEquals(5, bundle.getAssets().size());
        assertTrue(bundle.has(Asset.Type.JAVA_SCRIPT));
    }

    @Test
    void loadGoogle() throws IOException {
        AssetBundle bundle = assetBundleManager.getAssetBundle("google-cloud");
        assertEquals(1, bundle.getAssets().size());
        assertTrue(bundle.has(Asset.Type.JAVA_SCRIPT));
        assertFalse(bundle.has(Asset.Type.STYLE_SHEET));
        Iterator<Asset> iterator = bundle.getAssets().iterator();

        Asset asset = iterator.next();
        assertEquals("maps.googleapis.com", asset.getName());
        assertEquals("/maps/api/js", asset.getPath());
        assertEquals(0, asset.getOrder());
        assertEquals("text/javascript", asset.getContentType());
    }

    private void doLoad() {
        loader.load();
        assetBundleManager.loadDynamic();
    }


}