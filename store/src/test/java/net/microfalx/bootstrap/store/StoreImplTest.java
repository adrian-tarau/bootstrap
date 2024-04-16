package net.microfalx.bootstrap.store;

import lombok.*;
import net.microfalx.lang.Identifiable;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class StoreImplTest extends AbstractStoreTest {

    private static final AtomicInteger INDEX = new AtomicInteger(1);

    private Store<Item, String> store;

    void before() throws Exception {
        super.before();
        store = storeService.registerStore(Store.Options.create("Test " + System.currentTimeMillis() + "_" + INDEX.getAndIncrement()));
        store.clear();
    }

    @Test
    void create() {
        assertNotNull(store.getDirectory());
        assertNotNull(store.getOptions());
    }

    @Test
    void close() {
        ((StoreImpl) store).close();
    }

    @Test
    void add() {
        store.add(Item.builder().id(UUID.randomUUID().toString()).name("Test " + System.currentTimeMillis()).value(System.currentTimeMillis()).build());
        assertEquals(1, store.count());
    }

    @Test
    void find() {
        store.add(Item.builder().id(UUID.randomUUID().toString()).name("Test " + System.currentTimeMillis()).value(System.currentTimeMillis()).build());
        assertNull(store.find("a"));
        store.add(Item.builder().id("a").build());
        assertNotNull(store.find("a"));
    }

    @Test
    void delete() {
        Item item = Item.builder().id("a").build();
        store.add(item);
        assertNotNull(store.find("a"));
        store.remove(item);
        assertNull(store.find("a"));

        store.add(item);
        assertNotNull(store.find("a"));
        store.remove("a");
        assertNull(store.find("a"));
    }

    @Test
    void clear() {
        store.add(Item.builder().id("a").build());
        store.add(Item.builder().id("b").build());
        store.add(Item.builder().id("c").build());
        assertEquals(3, store.count());
        store.clear();
        assertEquals(0, store.count());
    }

    @Test
    void iterate() {
        assertFalse(store.iterator().hasNext());
        store.add(Item.builder().id("a").build());
        store.add(Item.builder().id("b").build());
        store.add(Item.builder().id("c").build());
        Iterator<Item> iterator = store.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next().getId());
        assertEquals("b", iterator.next().getId());
        assertEquals("c", iterator.next().getId());
        assertFalse(iterator.hasNext());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Item implements Identifiable<String> {

        private String id;
        private String name;
        private long value;

    }

}