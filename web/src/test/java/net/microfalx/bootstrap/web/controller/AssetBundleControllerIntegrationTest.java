package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.test.annotation.DisableJpa;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootConfiguration
@DisableJpa
@ComponentScan({"net.microfalx.bootstrap.web"})
public class AssetBundleControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void bootstrapJavaScript() throws Exception {
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/asset/js/bootstrap", String.class);
        assertEquals("text/javascript;charset=UTF-8", entity.getHeaders().getContentType().toString());
        Assertions.assertThat(entity.getBody()).contains("bootstrap");
    }

    @Test
    void bootstrapStylesheet() throws Exception {
        ResponseEntity<String> entity = testRestTemplate.getForEntity("/asset/css/bootstrap", String.class);
        assertEquals("text/css;charset=UTF-8", entity.getHeaders().getContentType().toString());
        Assertions.assertThat(entity.getBody()).contains("bootstrap");
    }
}
