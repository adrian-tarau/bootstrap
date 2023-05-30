package net.microfalx.bootstrap.web.application;

import com.google.common.collect.Iterators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AssetBundleLoaderTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private Application application;

    @InjectMocks
    private AssetBundleManager assetBundleManager;

    private AssetBundleLoader loader;

    @BeforeEach
     void setup() {
        //when(applicationService.getApplication()).thenReturn(application);
        loader = new AssetBundleLoader(assetBundleManager);
    }

    @Test
    void load() {
        loader.load();
        assertEquals(3, assetBundleManager.getAssetBundles().size());
    }

    @Test
    void loadJQuery() throws IOException {
        loader.load();
        AssetBundle bundle = assetBundleManager.getAssetBundle("jquery");
        assertEquals(4, bundle.getAssets().size());
        Iterator<Asset> iterator = bundle.getAssets().iterator();

        Asset asset = iterator.next();
        assertEquals("jquery_js", asset.getId());
        assertEquals("jquery.js", asset.getName());
        assertEquals("jquery.js", asset.getPath());
        assertEquals(0, asset.getOrder());
        assertEquals("text/javascript", asset.getContentType());
        assertTrue(asset.getResource().exists());

        Iterators.advance(iterator, 2);
        asset = iterator.next();
        assertEquals("jquery_datetimepicker_css", asset.getId());
        assertEquals("jquery.datetimepicker.css", asset.getName());
        assertEquals("jquery.datetimepicker.css", asset.getPath());
        assertEquals(3, asset.getOrder());
        assertEquals("text/css", asset.getContentType());
        assertFalse(asset.getResource().exists());
    }

    @Test
    void loadBootstrap() throws IOException {
        loader.load();
        AssetBundle bundle = assetBundleManager.getAssetBundle("bootstrap");
        assertEquals(2, bundle.getAssets().size());
    }


}