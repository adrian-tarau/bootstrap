package net.microfalx.bootstrap.ai.core.repository;

import net.microfalx.bootstrap.ai.api.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class InternetModelRepositoryTest {

    private final InternetModelRepository repository = new InternetModelRepository();
    private Model missingModel;
    private Model smallModel;

    @BeforeEach
    void setup() {
        missingModel = Model.create("Missing Model", "missing01")
                .downloadUri("https://huggingface.co/dummy.gguf?download=true")
                .build();
        smallModel = Model.create("Test Model", "test01")
                .downloadUri("https://huggingface.co/mradermacher/Tiny-Moe-GGUF/resolve/main/Tiny-Moe.Q4_K_M.gguf?download=true")
                .build();
    }

    @Test
    void supports() {
        assertTrue(repository.supports(smallModel));
    }

    @Test
    void resolve() throws IOException {
        assertNull(repository.resolve(missingModel));
        assertNotNull(repository.resolve(smallModel));
    }
}