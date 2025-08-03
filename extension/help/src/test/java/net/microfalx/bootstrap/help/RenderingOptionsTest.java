package net.microfalx.bootstrap.help;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderingOptionsTest {

    @Test
    void hashing() {
        assertEquals("1wl47ikkro6zt", RenderingOptions.DEFAULT.getHash());
        assertEquals("4fw5rczh0e4i", RenderingOptions.builder().heading(false).navigation(true).build().getHash());
        assertEquals("z90xt658l9ac", RenderingOptions.builder().heading(true).navigation(false).build().getHash());
        assertEquals("hw1meh0aka9y", RenderingOptions.builder().heading(true).navigation(true).build().getHash());

    }

}