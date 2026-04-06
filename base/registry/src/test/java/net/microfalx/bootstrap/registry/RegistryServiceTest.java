package net.microfalx.bootstrap.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class RegistryServiceTest {

    private RegistryService registryService;

    @BeforeEach
    void setup() throws Exception {
        List<Storage> storages = List.of(new MemoryStorage(), Mockito.mock(Storage.class));
        registryService = new RegistryService(storages);
        registryService.afterPropertiesSet();
    }

    @Test
    void getStorages() {
        assertEquals(2, registryService.getStorages().size());
    }

    @Test
    void getStorage() {
        assertSame(registryService.getStorage().getClass(), MemoryStorage.class);
    }

    @Test
    void getRegistry() {
        assertSame(registryService.getRegistry().getClass(), RegistryImpl.class);
    }

}