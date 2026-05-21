package net.microfalx.bootstrap.logger;

import net.microfalx.resource.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggerLoaderTest {

    @Test
    void loadClassPath() {
        LoggerLoader loader = new LoggerLoader();
        loader.load();
        assertEquals(2, loader.getAppenders().size());
    }

    @Test
    void loadAppender() throws IOException {
        LoggerLoader loader = new LoggerLoader();
        loader.load(ClassPathResource.file("logger1.xml"));
        assertEquals(3, loader.getAppenders().size());
        Iterator<Appender> iterator = loader.getAppenders().iterator();

        Appender appender = iterator.next();
        assertEquals("test1", appender.getId());
        assertEquals("Test1", appender.getName());
        assertEquals("test1.log", appender.getFileName());
        assertEquals(1, appender.getIncluded().size());
        assertEquals(0, appender.getExcluded().size());

        appender = iterator.next();
        assertEquals("test2", appender.getId());
        assertEquals("Test2", appender.getName());
        assertEquals("aaa.log", appender.getFileName());
        assertEquals(1, appender.getIncluded().size());
        assertEquals(1, appender.getExcluded().size());

        appender = iterator.next();
        assertEquals("test2", appender.getId());
        assertEquals("Test2", appender.getName());
        assertEquals("test2.log", appender.getFileName());
        assertEquals(1, appender.getIncluded().size());
        assertEquals(1, appender.getExcluded().size());
    }

}