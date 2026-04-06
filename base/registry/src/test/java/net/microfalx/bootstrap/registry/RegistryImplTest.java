package net.microfalx.bootstrap.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegistryImplTest {

    private Registry registry;

    @BeforeEach
    void setup() throws Exception {
        List<Storage> storages = List.of(new MemoryStorage(), Mockito.mock(Storage.class));
        RegistryService registryService = new RegistryService(storages);
        registryService.afterPropertiesSet();
        registry = registryService.getRegistry();
    }

    @Test
    void existsReturnsFalseForNonExistentPath() {
        assertFalse(registry.exists("/nonexistent"));
    }

    @Test
    void existsReturnsTrueForExistingPath() {
        Data data = Data.create("/existing");
        data.set("test");
        registry.set(data);
        assertTrue(registry.exists("/existing"));
    }

    @Test
    void getReturnsEmptyForNonExistentPath() {
        assertTrue(registry.get("/nonexistent").isEmpty());
    }

    @Test
    void testSetAndGet() {
        Data data = Data.create("/test");
        data.set("value");
        registry.set(data);
        Optional<Data> retrieved = registry.get("/test");
        assertTrue(retrieved.isPresent());
        assertEquals("value", retrieved.get().get());
    }

    @Test
    void getOrCreateCreatesNewData() {
        Data data = registry.getOrCreate("/new");
        assertNotNull(data);
        assertFalse(data.exists());
    }

    @Test
    void getOrCreateReturnsExisting() {
        Data original = Data.create("/existing");
        original.set("original");
        registry.set(original);
        Data retrieved = registry.getOrCreate("/existing");
        assertTrue(retrieved.exists());
        assertEquals("original", retrieved.get());
    }

    @Test
    void lookupReturnsEmptyForNonExistent() {
        assertTrue(registry.lookup("/nonexistent").isEmpty());
    }

    @Test
    void lookupReturnsNodeForExisting() {
        Data data = Data.create("/node");
        data.set("node");
        registry.set(data);
        Optional<Node> node = registry.lookup("/node");
        assertTrue(node.isPresent());
        assertEquals("/node", node.get().getPath());
    }

    @Test
    void listReturnsEmptyForNonExistentPath() {
        Iterable<Data> list = registry.list("/nonexistent");
        assertFalse(list.iterator().hasNext());
    }

    @Test
    void listReturnsDataForExistingPath() {
        Data data1 = Data.create("/parent/child1");
        data1.set("child1");
        registry.set(data1);
        Data data2 = Data.create("/parent/child2");
        data2.set("child2");
        registry.set(data2);
        Iterable<Data> list = registry.list("/parent");
        List<Data> dataList = new java.util.ArrayList<>();
        list.forEach(dataList::add);
        assertEquals(2, dataList.size());
    }

    @Test
    void walk() {
        Data data = new DataImpl("/walk");
        data.set("walk");
        registry.set(data);
        boolean[] visited = {false};
        registry.walk("/walk", 1, (path, node) -> {
            visited[0] = true;
            return true;
        });
        assertTrue(visited[0]);
    }

}