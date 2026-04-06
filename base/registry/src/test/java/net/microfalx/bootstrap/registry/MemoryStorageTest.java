package net.microfalx.bootstrap.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MemoryStorageTest {

    private MemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new MemoryStorage();
    }

    // Tests for put() and get()

    @Test
    void putStoresData() {
        byte[] data = "test data".getBytes();
        storage.put("/test", data);

        byte[] retrieved = storage.get("/test");
        assertArrayEquals(data, retrieved);
    }

    @Test
    void putUpdatesExistingData() {
        byte[] data1 = "first data".getBytes();
        byte[] data2 = "second data".getBytes();

        storage.put("/test", data1);
        storage.put("/test", data2);

        byte[] retrieved = storage.get("/test");
        assertArrayEquals(data2, retrieved);
    }

    @Test
    void putNormalizesPath() {
        byte[] data = "test data".getBytes();
        storage.put("/Test Path/Item", data);

        byte[] retrieved = storage.get("/test_path/item");
        assertArrayEquals(data, retrieved);
    }

    @Test
    void putWithVersionSetsVersion() {
        byte[] data = "test data".getBytes();
        storage.put("/test", data, 5);

        Optional<Node> node = storage.getNode("/test");
        assertTrue(node.isPresent());
        assertEquals(5, node.get().getVersion());
    }

    @Test
    void putWithoutVersionIncrementsVersion() {
        byte[] data = "test data".getBytes();
        storage.put("/test", data);

        Optional<Node> node = storage.getNode("/test");
        assertEquals(1, node.get().getVersion());

        storage.put("/test", data);
        node = storage.getNode("/test");
        assertEquals(2, node.get().getVersion());
    }

    @Test
    void getReturnsNullForNonExistentPath() {
        byte[] result = storage.get("/nonexistent");
        assertNull(result);
    }

    @Test
    void getNormalizesPath() {
        byte[] data = "test data".getBytes();
        storage.put("/Test/Path", data);

        byte[] retrieved = storage.get("/test/path");
        assertArrayEquals(data, retrieved);
    }

    @Test
    void getReturnsEmptyData() {
        byte[] data = new byte[0];
        storage.put("/test", data);

        byte[] retrieved = storage.get("/test");
        assertArrayEquals(data, retrieved);
        assertEquals(0, retrieved.length);
    }

    // Tests for exists()

    @Test
    void existsReturnsTrueForExistingPath() {
        storage.put("/test", "data".getBytes());
        assertTrue(storage.exists("/test"));
    }

    @Test
    void existsReturnsFalseForNonExistentPath() {
        assertFalse(storage.exists("/nonexistent"));
    }

    @Test
    void existsNormalizesPath() {
        storage.put("/Test/Path", "data".getBytes());
        assertTrue(storage.exists("/test/path"));
    }

    // Tests for getNode()

    @Test
    void getNodeReturnsNodeForExistingPath() {
        storage.put("/test", "data".getBytes());
        Optional<Node> node = storage.getNode("/test");

        assertTrue(node.isPresent());
        assertTrue(node.get().exists());
        assertTrue(node.get().isLeaf());
    }

    @Test
    void getNodeReturnsEmptyOptionalForNonExistentPath() {
        Optional<Node> node = storage.getNode("/nonexistent");
        assertTrue(node.isEmpty());
    }

    @Test
    void getNodeNormalizesPath() {
        storage.put("/Test/Path", "data".getBytes());
        Optional<Node> node = storage.getNode("/test/path");

        assertTrue(node.isPresent());
    }

    @Test
    void getNodeReturnsNodeWithCorrectMetadata() {
        byte[] data = "test data".getBytes();
        storage.put("/test", data);

        Optional<Node> node = storage.getNode("/test");
        assertTrue(node.isPresent());

        Node n = node.get();
        assertTrue(n.exists());
        assertTrue(n.isLeaf());
        assertEquals(1, n.getVersion());
        assertEquals(1, n.getUpdateCount());
        assertNotNull(n.getCreatedAt());
        assertNotNull(n.getUpdatedAt());
    }

    @Test
    void getNodeTrackesUpdateCount() {
        storage.put("/test", "data1".getBytes());
        assertEquals(1, storage.getNode("/test").get().getUpdateCount());

        storage.put("/test", "data2".getBytes());
        assertEquals(2, storage.getNode("/test").get().getUpdateCount());

        storage.put("/test", "data3".getBytes());
        assertEquals(3, storage.getNode("/test").get().getUpdateCount());
    }

    // Tests for getChildren()

    @Test
    void getChildrenNonRecursive() {
        storage.put("/parent/child1", "data1".getBytes());
        storage.put("/parent/child2", "data2".getBytes());
        storage.put("/parent/grandchild/item", "data3".getBytes());

        Collection<Node> children = storage.getChildren("/parent", false);

        assertEquals(2, children.size());
    }

    @Test
    void getChildrenRecursive() {
        storage.put("/parent/child1", "data1".getBytes());
        storage.put("/parent/child2", "data2".getBytes());
        storage.put("/parent/grandchild/item", "data3".getBytes());
        storage.put("/parent/grandchild/deep/item", "data4".getBytes());

        Collection<Node> children = storage.getChildren("/parent", true);

        assertEquals(4, children.size());
    }

    @Test
    void getChildrenEmpty() {
        Collection<Node> children = storage.getChildren("/nonexistent", false);
        assertTrue(children.isEmpty());
    }

    @Test
    void getChildrenDoesNotIncludeParent() {
        storage.put("/parent", "parent data".getBytes());
        storage.put("/parent/child", "child data".getBytes());

        Collection<Node> children = storage.getChildren("/parent", false);

        assertEquals(1, children.size());
    }

    @Test
    void getChildrenNormalizesPath() {
        storage.put("/Parent/Child1", "data1".getBytes());
        storage.put("/Parent/Child2", "data2".getBytes());

        Collection<Node> children = storage.getChildren("/parent", false);

        assertEquals(2, children.size());
    }

    @Test
    void getChildrenRootPath() {
        storage.put("/item1", "data1".getBytes());
        storage.put("/item2", "data2".getBytes());
        storage.put("/folder/item3", "data3".getBytes());

        Collection<Node> children = storage.getChildren("/", false);

        assertEquals(2, children.size());
    }

    @Test
    void getChildrenRecursiveMultipleLevels() {
        storage.put("/a/b/c/d", "data".getBytes());
        storage.put("/a/b/e", "data".getBytes());
        storage.put("/a/f", "data".getBytes());

        Collection<Node> children = storage.getChildren("/a", true);

        assertEquals(3, children.size());
    }

    @Test
    void getChildrenSimilarPaths() {
        storage.put("/user", "data1".getBytes());
        storage.put("/user/profile", "data2".getBytes());
        storage.put("/username", "data3".getBytes());

        Collection<Node> children = storage.getChildren("/user", false);

        assertEquals(1, children.size());
    }

    // Tests for concurrent operations (thread safety)

    @Test
    void threadSafePutAndGet() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                storage.put("/path" + i, ("data" + i).getBytes());
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                storage.get("/path" + i);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Verify all data was stored correctly
        for (int i = 0; i < 100; i++) {
            byte[] result = storage.get("/path" + i);
            assertArrayEquals(("data" + i).getBytes(), result);
        }
    }

    // Tests for edge cases

    @Test
    void putEmptyPath() {
        storage.put("", "data".getBytes());
        byte[] result = storage.get("/");
        assertArrayEquals("data".getBytes(), result);
    }

    @Test
    void putLargeData() {
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        storage.put("/large", largeData);
        byte[] retrieved = storage.get("/large");

        assertArrayEquals(largeData, retrieved);
    }

    @Test
    void putPathsWithSpecialCharacters() {
        byte[] data = "data".getBytes();
        storage.put("/my-resource/sub_item", data);

        assertTrue(storage.exists("/my_resource/sub_item"));
    }

    @Test
    void nodeHasTimestamps() {
        storage.put("/test", "data".getBytes());
        Optional<Node> node = storage.getNode("/test");

        assertTrue(node.isPresent());
        assertNotNull(node.get().getCreatedAt());
        assertNotNull(node.get().getUpdatedAt());
    }

    @Test
    void multipleButsPreserveCreatedTime() throws InterruptedException {
        storage.put("/test", "data1".getBytes());
        Optional<Node> node1 = storage.getNode("/test");
        var createdAt = node1.get().getCreatedAt();

        Thread.sleep(10);

        storage.put("/test", "data2".getBytes());
        Optional<Node> node2 = storage.getNode("/test");
        var updatedAt = node2.get().getUpdatedAt();

        assertEquals(createdAt, node1.get().getCreatedAt());
        assertTrue(updatedAt.isAfter(createdAt) || updatedAt.isEqual(createdAt));
    }

    @Test
    void getChildrenRecursiveFromRoot() {
        storage.put("/a/b/c", "data".getBytes());
        storage.put("/x/y", "data".getBytes());

        Collection<Node> children = storage.getChildren("/", true);

        assertTrue(children.size() >= 2);
    }

}