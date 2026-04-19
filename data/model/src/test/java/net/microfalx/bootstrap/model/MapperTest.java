package net.microfalx.bootstrap.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Subject;
import net.microfalx.lang.annotation.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTest extends ServiceUnitTestCase {

    @Mock
    private ApplicationContext applicationContext;

    @Subject
    private MetadataService metadataService;

    @BeforeEach
    void before() throws Exception {
        Mapper.builder(SimpleSource.class, SimpleDestination.class).mapper(new SimpleMapper()).register();
    }

    @Test
    void simpleTypes() {
        SimpleDestination destination = Mapper.get(SimpleSource.class, SimpleDestination.class).to(new SimpleSource().setName("John").setAge(30).setType(Type.T1).setDummy1(100));
        assertEquals("John", destination.getName());
        assertEquals(30, destination.getAge());
        assertEquals(Type.T1, destination.getType());
        assertEquals(0, destination.getDummy2());
        destination = new SimpleDestination();
        Mapper.get(SimpleSource.class, SimpleDestination.class).copy(new SimpleSource().setName("John").setAge(30).setDummy1(100), destination);
        assertEquals("John", destination.getName());
        assertEquals(30, destination.getAge());
        assertEquals(0, destination.getDummy2());
    }

    @Test
    void jpa() {
        PersonJpa personJpa = Mapper.get(Person.class, PersonJpa.class).to(new Person().setId(1).setFirstName("John").setLastName("Doe").setAge(30).setAge(100));
        assertEquals(1, personJpa.getId());
        assertEquals("John", personJpa.getFirstName());
        assertEquals("Doe", personJpa.getLastName());
        assertEquals(100, personJpa.getAge());
    }

    public enum Type {
        T1, T2
    }

    @Getter
    @Setter
    @ToString
    public static class SimpleSource {

        private String name;
        private Type type;
        private int age;
        private int dummy1;
    }

    @Getter
    @Setter
    @ToString
    public static class SimpleDestination {

        private String name;
        private Type type;
        private int age;
        private int dummy2;

    }

    @Provider
    public static class TestMapperProvider implements MapperProvider {

        @Override
        public void onInitializing() {
            Mapper.builder(Person.class, PersonJpa.class).mapper(new PersonMapper()).register();
        }
    }

    private static class SimpleMapper implements BiConsumer<SimpleSource, SimpleDestination> {

        @Override
        public void accept(SimpleSource simpleSource, SimpleDestination simpleDestination) {

        }
    }

    private static class PersonMapper implements BiConsumer<Person, PersonJpa> {

        @Override
        public void accept(Person simpleSource, PersonJpa simpleDestination) {

        }
    }
}