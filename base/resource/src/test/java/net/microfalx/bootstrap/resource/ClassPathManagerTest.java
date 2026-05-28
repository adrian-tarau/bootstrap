package net.microfalx.bootstrap.resource;

import net.microfalx.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ClassPathManagerTest {

    private ClassPathManager manager = new ClassPathManager();

    @BeforeEach
    void setup() {
        manager.initialize();
    }

    @Test
    void getDirectories() {
        assertThat(manager.getDirectories().size()).isGreaterThan(0);
    }

    @Test
    void getResources() {
        assertThat(manager.getResources().size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void resolve() throws IOException {
        Resource resolve = manager.resolve(URI.create("classpath:/logger.xml"));
        assertThat(resolve.exists()).isTrue();
        assertThat(resolve.toURI().getScheme()).isEqualTo("file");

        resolve = manager.resolve(URI.create("classpath:/test.xml"));
        assertThat(resolve.exists()).isFalse();
        assertThat(resolve.toURI().getPath()).endsWith("null");
    }

}