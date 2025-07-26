package net.microfalx.bootstrap.web.application;

import net.microfalx.bootstrap.web.component.Menu;
import net.microfalx.bootstrap.web.container.WebContainerService;
import net.microfalx.lang.StringUtils;
import net.microfalx.resource.Resource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private WebContainerService webContainerService;

    @Spy
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void before() throws Exception {
        applicationService.afterPropertiesSet();
    }

    @Test
    void getApplication() {
        assertNotNull(applicationService.getApplication());
        assertEquals("Default", applicationService.getApplication().getName());
        assertEquals("The default application descriptor", applicationService.getApplication().getDescription());
    }

    @Test
    void navigation() {
        assertEquals(2, applicationService.getNavigations().size());
        applicationService.registerNavigation(new Menu().setId("test"));
        assertEquals(3, applicationService.getNavigations().size());
        assertNotNull(applicationService.getNavigation("test"));
    }

    @Test
    void assetBundle() {
        assertEquals(3, applicationService.getAssetBundles().size());
        applicationService.registerAssetBundle(AssetBundle.builder("test")
                .asset(Asset.file(Asset.Type.JAVA_SCRIPT, "test.js").build())
                .build());
        assertEquals(4, applicationService.getAssetBundles().size());
        assertEquals(4, applicationService.getAssetBundle("jquery").getAssets().size());
    }

    @Test
    void assetBundleContent() throws IOException {
        Resource resource = applicationService.getAssetBundleContent(Asset.Type.JAVA_SCRIPT, "jquery");
        Assertions.assertThat(resource.loadAsString()).contains("Asset: jquery.js").contains("jQuery JavaScript Library")
                .contains("Asset: jquery.datetimepicker.js");
        resource = applicationService.getAssetBundleContent(Asset.Type.STYLE_SHEET, "jquery");
        Assertions.assertThat(resource.loadAsString()).contains("Asset: jquery.datetimepicker.css");
    }

    @Test
    void stylesheets() {
        mockWebContainer();
        String stylesheets = applicationService.getStylesheets(0, "jquery");
        Assertions.assertThat(stylesheets).contains("<link").contains("/asset/stylesheet/jquery?version=3.6.2");
        stylesheets = applicationService.getStylesheets(0);
        Assertions.assertThat(stylesheets).contains("<link").contains("/asset/stylesheet/jquery?version=3.6.2");
        Assertions.assertThat(stylesheets).contains("<link").contains("/asset/stylesheet/bootstrap");
        Assertions.assertThat(stylesheets).contains("<link").contains("/asset/stylesheet/glyphs");
    }

    @Test
    void scripts() {
        mockWebContainer();
        String scripts = applicationService.getScripts(0, "jquery");
        Assertions.assertThat(scripts).contains("<script").contains("/asset/script/jquery?version=3.6.2");
        scripts = applicationService.getScripts(0);
        Assertions.assertThat(scripts).contains("<script").contains("/asset/script/jquery?version=3.6.2");
        Assertions.assertThat(scripts).contains("<script").contains("/asset/script/bootstrap");
        Assertions.assertThat(scripts).contains("<script").contains("/asset/script/glyphs");
    }

    private void mockWebContainer() {
        doAnswer(invocation -> StringUtils.addStartSlash(invocation.getArgument(0))).when(webContainerService).getPath(anyString());
    }
}