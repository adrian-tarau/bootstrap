package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.ApplicationProperties;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.container.WebContainerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {AssetBundleController.class, WebContainerService.class, ApplicationService.class, ApplicationProperties.class})
@WebMvcTest(AssetBundleController.class)
class AssetBundleControllerTest extends AbstractControllerTestCase {

    @Autowired
    private AssetBundleController controller;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bootstrapJavaScript() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/asset/js/bootstrap").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "text/javascript;charset=UTF-8"))
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).contains("Asset: bootstrap.js");
    }

    @Test
    void bootstrapStylesheet() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/asset/css/bootstrap").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "text/css;charset=UTF-8"))
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).contains("Asset: bootstrap.css");
    }

}