package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.application.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {HomeController.class})
@WebMvcTest(HomeController.class)
class HomeControllerTest extends AbstractControllerTestCase {

    @Autowired
    private HomeController controller;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void defaultPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).contains("Home").doesNotContain("Parent 2");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void defaultPageAsAdmin() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).contains("Home").contains("Parent 2");
    }

}