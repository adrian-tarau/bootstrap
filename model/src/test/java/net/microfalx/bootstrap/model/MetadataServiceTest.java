package net.microfalx.bootstrap.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import net.microfalx.lang.Id;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @InjectMocks
    private MetadataService metadataService;

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
        Metadata<Person, Field<Person>> metadata = metadataService.getMetadata(Person.class);
        assertNotNull(metadata);
        assertSame(metadata.getClass(), PojoMetadataProvider.DefaultPojoMetadata.class);
        assertEquals(1, metadata.getIdFields().size());
        assertEquals("id", metadata.findIdField().getName());
        assertEquals(5, metadata.getFields().size());
    }

    @Test
    void loadJpaMetadata() {
        Metadata<PersonJpa, Field<PersonJpa>> metadata = metadataService.getMetadata(PersonJpa.class);
        assertNotNull(metadata);
        assertSame(metadata.getClass(), JpaMetadata.class);
        assertEquals(2, metadata.getIdFields().size());
        Assertions.assertThatCode(() -> metadata.findIdField().getName()).hasMessageContaining("Multiple");
        assertEquals(5, metadata.getFields().size());
    }

    @Entity
    @Table(name = "person")
    private static class PersonJpa {

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

    private static class Person {

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
    }

}