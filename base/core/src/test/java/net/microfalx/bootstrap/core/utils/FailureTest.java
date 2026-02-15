package net.microfalx.bootstrap.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FailureTest {

    @BeforeEach
    void setup() {
        Failure.reset();
    }

    @Test
    void getRootCauseName() {
        assertEquals("I/O", Failure.of(new IOException("Bad")).getRootCauseName());
    }

    @Test
    void getRootCauseMessage() {
        assertEquals("Bad", Failure.of(new IOException("Bad")).getRootCauseMessage());
        assertEquals("Bad", Failure.of(new IOException("Really Bad", new IOException("Bad"))).getRootCauseMessage());
    }

    @Test
    void getRootCauseDescription() {
        assertEquals("Bad (I/O)", Failure.of(new IOException("Bad")).getRootCauseDescription());
        assertEquals("Bad (Security)", Failure.of(new IOException("Really Bad", new SecurityException("Bad"))).getRootCauseDescription());
    }

    @Test
    void contains() {
        assertTrue(Failure.of(new IOException("Bad")).contains(IOException.class));
        assertTrue(Failure.of(new SecurityException("Really bad", new IOException("Bad"))).contains(SecurityException.class));
        assertFalse(Failure.of(new IOException("Bad")).contains(SecurityException.class));
    }

    @Test
    void registerType() {
        Failure.registerType(Failure.Type.ILLEGAL_INPUT, MyException.class);
        assertEquals(Failure.Type.INTERNAL_ERROR, Failure.getType(new MySubException("Bad")));
        assertEquals(Failure.Type.ILLEGAL_INPUT, Failure.getType(new MyException("Bad")));
        assertEquals(Failure.Type.RESOURCE_NOT_FOUND, Failure.getType(new FileNotFoundException("Bad")));
    }

    @Test
    void registerSubType() {
        Failure.registerSubType(Failure.Type.CORRUPTED_DATA, MyException.class);
        assertEquals(Failure.Type.CORRUPTED_DATA, Failure.getType(new MySubException("Bad")));
        assertEquals(Failure.Type.CORRUPTED_DATA, Failure.getType(new MyException("Bad")));
    }

    public static class MyException extends RuntimeException {

        public MyException(String message) {
            super(message);
        }

        public MyException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MySubException extends MyException {
        public MySubException(String message) {
            super(message);
        }

        public MySubException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}