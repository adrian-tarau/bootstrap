package net.microfalx.bootstrap.web.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
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
