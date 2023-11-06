package net.microfalx.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.lang.annotation.I18n;
import net.microfalx.lang.annotation.Id;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @InjectMocks
    private MetadataService metadataService;

    @Spy
    private I18nService i18nService;

    @BeforeEach
    void before() {
        metadataService.initialize();
    }

    @Test
    void loadProviders() {
        assertEquals(2, metadataService.getProviders().size());
    }

    @Test
    void loadPojoMetadata() {
        Metadata<Person, Field<Person>, Integer> metadata = metadataService.getMetadata(Person.class);
        assertNotNull(metadata);
        assertSame(metadata.getClass(), PojoMetadataProvider.DefaultPojoMetadata.class);
        assertEquals(1, metadata.getIdFields().size());
        assertEquals("id", metadata.findIdField().getName());
        assertEquals(5, metadata.getFields().size());
    }

    @Test
    void loadJpaMetadata() {
        Metadata<PersonJpa, Field<PersonJpa>, Integer> metadata = metadataService.getMetadata(PersonJpa.class);
        assertNotNull(metadata);
        assertSame(metadata.getClass(), JpaMetadata.class);
        assertEquals(2, metadata.getIdFields().size());
        Assertions.assertThatCode(() -> metadata.findIdField().getName()).hasMessageContaining("Multiple");
        assertEquals(6, metadata.getFields().size());
    }

    @Test
    void loadI18n() {
        Metadata<Person, Field<Person>, Integer> metadata = metadataService.getMetadata(Person.class);
        assertI18n(metadata);
    }

    @Test
    void loadI18nJpa() {
        Metadata<PersonJpa, Field<PersonJpa>, Integer> metadata = metadataService.getMetadata(PersonJpa.class);
        assertI18n(metadata);
    }

    @Test
    void getFieldValue() {
        Person person = createPerson();
        Metadata<Person, Field<Person>, Integer> metadata = metadataService.getMetadata(Person.class);
        Field<Person> field = metadata.get("firstName");
        assertEquals("John", field.get(person));
    }

    @Test
    void identical() {
        Person person1 = createPerson();
        Person person2 = createPerson();
        Metadata<Person, Field<Person>, Integer> metadata = metadataService.getMetadata(Person.class);
        assertTrue(metadata.identical(null, null));
        assertFalse(metadata.identical(person1, null));
        assertFalse(metadata.identical(null, person2));
        assertTrue(metadata.identical(person1, person2));
        person1.setAge(10);
        assertFalse(metadata.identical(person1, person2));
        person1.setAge(20);
        person2.setLastName("demo");
        assertFalse(metadata.identical(person1, person2));
    }

    @Test
    void copy() {
        Person person1 = createPerson();
        Metadata<Person, Field<Person>, Integer> metadata = metadataService.getMetadata(Person.class);
        Person person2 = metadata.copy(person1);
        assertTrue(metadata.identical(person1, person2));
    }

    private void assertI18n(Metadata<?, ? extends Field<?>, ?> metadata) {
        assertEquals("Person", metadata.getName());
        assertEquals("A person", metadata.getDescription());
        Field<?> field = metadata.get("id");
        assertEquals("Id", field.getLabel());
        assertEquals("The identifier of the person", field.getDescription());
        field = metadata.get("firstname");
        assertEquals("First Name", field.getLabel());
        assertEquals("The first name of the person", field.getDescription());
    }

    private Person createPerson() {
        Person person = new Person();
        person.setId(1);
        person.setFirstName("John");
        person.setLastName("Doe");
        person.setAge(20);
        return person;
    }

    @Entity
    @Table(name = "person")
    @I18n("person")
    public static class PersonJpa {

        private int id;

        @jakarta.persistence.Id
        @Column(name = "first_name")
        private String firstName;

        @jakarta.persistence.Id
        @Column(name = "last_name")
        private String lastName;

        @Column(name = "description")
        private String description;

        @Column(name = "age")
        private int age;

        @Transient
        private Double dummy;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Double getDummy() {
            return dummy;
        }

        public void setDummy(Double dummy) {
            this.dummy = dummy;
        }
    }

    public static class Person {

        @Id
        private int id;
        private String firstName;
        private String lastName;
        private String description;
        private int age;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, lastName, age);
        }
    }

}