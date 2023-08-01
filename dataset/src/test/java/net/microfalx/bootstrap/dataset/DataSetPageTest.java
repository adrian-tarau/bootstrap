package net.microfalx.bootstrap.dataset;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataSetPageTest {

    private Pageable SMALL = Pageable.ofSize(20);
    private Pageable LARGE = Pageable.ofSize(100);

    @Test
    void initialize() {
        DataSetPage<String> page = new DataSetPage<>(SMALL.withPage(1), createResult(23));
        assertNotNull(page.getPageable());
        assertNotNull(page.getSort());
    }

    @Test
    void emptyPage() {
        DataSetPage<String> page = new DataSetPage<>(SMALL, createResult(0));
        assertEquals(1, page.getTotalPages());
        assertEquals(0, page.getTotalElements());
        assertEquals(0, page.getNumber());
        assertEquals(20, page.getSize());
        assertEquals(0, page.getNumberOfElements());
        assertEquals(0, page.getContent().size());

        assertFalse(page.hasContent());
        assertTrue(page.isEmpty());
        assertFalse(page.hasPrevious());
        assertFalse(page.hasNext());
        assertTrue(page.isFirst());
        assertTrue(page.isLast());

        assertEquals(0, page.nextPageable().getPageNumber());
        assertEquals(0, page.previousPageable().getPageNumber());
    }

    @Test
    void first() {
        DataSetPage<String> page = new DataSetPage<>(SMALL, createResult(23));
        assertEquals(2, page.getTotalPages());
        assertEquals(23, page.getTotalElements());
        assertEquals(0, page.getNumber());
        assertEquals(20, page.getSize());
        assertEquals(20, page.getNumberOfElements());
        assertEquals(20, page.getContent().size());

        assertTrue(page.hasContent());
        assertFalse(page.isEmpty());
        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
        assertTrue(page.isFirst());
        assertFalse(page.isLast());

        assertEquals(1, page.nextPageable().getPageNumber());
        assertEquals(0, page.previousPageable().getPageNumber());
    }

    @Test
    void middle() {
        DataSetPage<String> page = new DataSetPage<>(SMALL.withPage(1), createResult(53));
        assertEquals(3, page.getTotalPages());
        assertEquals(53, page.getTotalElements());
        assertEquals(1, page.getNumber());
        assertEquals(20, page.getSize());
        assertEquals(20, page.getNumberOfElements());
        assertEquals(20, page.getContent().size());

        assertTrue(page.hasContent());
        assertFalse(page.isEmpty());
        assertTrue(page.hasPrevious());
        assertTrue(page.hasNext());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());

        assertEquals(2, page.nextPageable().getPageNumber());
        assertEquals(0, page.previousPageable().getPageNumber());
    }

    @Test
    void last() {
        DataSetPage<String> page = new DataSetPage<>(SMALL.withPage(2), createResult(53));
        assertEquals(3, page.getTotalPages());
        assertEquals(53, page.getTotalElements());
        assertEquals(2, page.getNumber());
        assertEquals(20, page.getSize());
        assertEquals(13, page.getNumberOfElements());
        assertEquals(13, page.getContent().size());

        assertTrue(page.hasContent());
        assertFalse(page.isEmpty());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext());
        assertFalse(page.isFirst());
        assertTrue(page.isLast());

        assertEquals(2, page.nextPageable().getPageNumber());
        assertEquals(1, page.previousPageable().getPageNumber());
    }

    private List<String> createResult(int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add("Item" + (i + 1));
        }
        return list;
    }

}