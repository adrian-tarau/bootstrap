package net.microfalx.bootstrap.model;

import net.microfalx.bootstrap.core.i18n.I18nService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

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

    @Test
    void collection() {
        Metadata<Person, Field<Person>, Integer> metadata = metadataService.getMetadata(Person.class);
        Field<Person> field = metadata.get("orders");
        assertEquals(Field.DataType.COLLECTION, field.getDataType());
        assertEquals(Collection.class, field.getDataClass());
        assertEquals(Order.class, field.getGenericDataClass());
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
}