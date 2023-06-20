package net.microfalx.bootstrap.web.search;

import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.search.SearchProperties;
import net.microfalx.bootstrap.search.SearchService;
import net.microfalx.bootstrap.web.application.ApplicationService;
import net.microfalx.bootstrap.web.controller.AbstractControllerTestCase;
import net.microfalx.bootstrap.web.dataset.DataSetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {SearchController.class, SearchService.class, SearchProperties.class})
@WebMvcTest(SearchController.class)
class SearchControllerTest extends AbstractControllerTestCase {

    @Spy
    private MetadataService metadataService;

    @InjectMocks
    private DataSetService dataSetService;

    @Mock
    private SearchService searchService;

    @BeforeEach
    void before() throws Exception {
        metadataService.afterPropertiesSet();
        dataSetService.afterPropertiesSet();
    }

    @Autowired
    private SearchController controller;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void defaultPage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/search").with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).contains("Home").doesNotContain("Parent 2");
    }

}