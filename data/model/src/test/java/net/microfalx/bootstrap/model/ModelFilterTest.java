package net.microfalx.bootstrap.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.microfalx.bootstrap.model.ComparisonExpression.*;
import static net.microfalx.bootstrap.model.Filter.create;
import static net.microfalx.bootstrap.model.LogicalExpression.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ModelFilterTest {

    @InjectMocks
    private MetadataService metadataService;
    private List<Person> persons = new ArrayList<>();

    @BeforeEach
    void setup() {
        metadataService.initialize();
        createPersons();
    }

    @Test
    void emptyFiler() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create());
        List<Person> result = modelFilter.toList();
        assertTrue(create().isEmpty());
        assertIterableEquals(persons, result);
        assertNotNull(modelFilter.toString());
        assertEquals("()", create().getDescription());
        assertEquals("and(expr=0)", create().getName());
    }

    @Test
    void filterWithEqualExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(eq("age", 5)));
        List<Person> result = modelFilter.toList();
        assertEquals(persons.get(2), result.iterator().next());
        assertNotNull(modelFilter.toString());
        assertEquals("age = 5", create(eq("age", 5)).getDescription());
        assertEquals("age", create(eq("age", 5)).getName());
        assertTrue(create(eq("age", 5)).equals(create(eq("age", 5))));
    }

    @Test
    void filterWithNotEqualExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(ne("age", 5)));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(List.of(persons.get(0), persons.get(1)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("age <> 5", create(ne("age", 5)).getExpression().getDescription());
        assertEquals("age", create(ne("age", 5)).getExpression().getName());
    }

    @Test
    void filterWithContainExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(contains("firstName", "J")));
        List<Person> result = modelFilter.toList();
        modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(contains("firstName", "Jo")));
        result = modelFilter.toList();
        assertIterableEquals(List.of(persons.get(0), persons.get(2)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("firstName contains 'J'", create(contains("firstName", "J")).getExpression().
                getDescription());
        assertEquals("firstName", create(contains("firstName", "J")).getExpression().getName());
    }

    @Test
    void filterWithBetweenExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(between("age", 9, 15)));
        List<Person> result = modelFilter.toList();
        assertEquals(persons.get(0), result.iterator().next());
        assertNotNull(modelFilter.toString());
        assertEquals("age between [9, 15]", create(between("age", 9, 15)).getExpression().
                getDescription());
        assertEquals("age", create(between("age", 9, 15)).getExpression().getName());
    }

    @Test
    void filterWithGreaterExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(gt("age", 100)));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(Collections.emptyList(), result);
        assertNotNull(modelFilter.toString());
        assertEquals("age > 100", create(gt("age", 100)).getExpression().
                getDescription());
        assertEquals("age", create(gt("age", 100)).getExpression().getName());
    }

    @Test
    void filterWithGreaterThanOrEqualExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(ge("age", 10)));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(List.of(persons.get(0), persons.get(1)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("age >= 10", create(ge("age", 10)).getExpression().
                getDescription());
        assertEquals("age", create(ge("age", 10)).getExpression().getName());
    }

    @Test
    void filterWithLessExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(lt("age", 90)));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(List.of(persons.get(0), persons.get(2)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("age < 90", create(lt("age", 90)).getExpression().
                getDescription());
        assertEquals("age", create(lt("age", 90)).getExpression().getName());
    }

    @Test
    void filterWithLessThanOrEqualExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(le("age", 90)));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(persons, result);
        assertNotNull(modelFilter.toString());
        assertEquals("age <= 90", create(le("age", 90)).getExpression().
                getDescription());
        assertEquals("age", create(le("age", 90)).getExpression().getName());
    }

    @Test
    void filterWithNullExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(isNull("firstName")));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(Collections.emptyList(), result);
        assertNotNull(modelFilter.toString());
        assertEquals("firstName is null", create(isNull("firstName")).getExpression().
                getDescription());
        assertEquals("firstName", create(isNull("firstName")).getExpression().getName());
    }

    @Test
    void filterWithNotNullExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(isNotNull("lastName")));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(persons, result);
        assertNotNull(modelFilter.toString());
        assertEquals("lastName not null", create(isNotNull("lastName")).getExpression().
                getDescription());
        assertEquals("lastName", create(isNotNull("lastName")).getExpression().getName());
    }

    @Test
    void filterWithLikeExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(like("description", "*year old*")));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(persons, result);
        assertNotNull(modelFilter.toString());
        assertEquals("description like '*year old*'", create(like("description", "*year old*")).
                getExpression().getDescription());
        assertEquals("description", create(like("description", "*year old*")).
                getExpression().getName());
    }

    @Test
    void filterWithRegexExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(regex("description", ".*?year old.*?")));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(persons, result);
        assertNotNull(modelFilter.toString());
        assertEquals("description regex '.*?year old.*?'", create(
                regex("description", ".*?year old.*?")).getExpression().getDescription());
        assertEquals("description", create(regex("description", ".*?year old.*?")).
                getExpression().getName());
    }

    @Test
    void filterWithInExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(in("firstName",
                "Sid", "Bob", "John")));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(List.of(persons.get(0), persons.get(2)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("firstName in [Sid, Bob, John]", create(in("firstName",
                "Sid", "Bob", "John")).getExpression().getDescription());
        assertEquals("firstName", create(in("firstName", "Sid", "Bob", "John")).
                getExpression().getName());
    }

    @Test
    void filterWithNotInExpression() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(notIn("lastName",
                "Bond", "Frost", "Rogers")));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(List.of(persons.get(0), persons.get(1)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("lastName not in [Bond, Frost, Rogers]", create(notIn("lastName",
                "Bond", "Frost", "Rogers")).getExpression().getDescription());
        assertEquals("lastName", create(notIn("lastName",
                "Bond", "Frost", "Rogers")).
                getExpression().getName());
    }

    @Test
    void twoComparisonsWithAnd() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(and(in("firstName",
                "Sid", "Bob", "John"), eq("age", 5))));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(Collections.singletonList(persons.get(2)), result);
        assertNotNull(modelFilter.toString());
        assertEquals("(firstName in [Sid, Bob, John] && age = 5)", create(and(in("firstName",
                "Sid", "Bob", "John"), eq("age", 5))).getDescription());
        assertEquals("and(expr=2)", create(and(in("firstName",
                "Sid", "Bob", "John"), eq("age", 5))).getName());
        assertFalse(in("firstName", "Sid", "Bob", "John").equals(eq("age", 5)));
    }

    @Test
    void twoComparisonsWithOr() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(or(in("firstName",
                "Sid", "Bob", "John"), eq("age", 90))));
        List<Person> result = modelFilter.toList();
        assertIterableEquals(persons, result);
        assertNotNull(modelFilter.toString());
        assertEquals("(firstName in [Sid, Bob, John] || age = 90)",create(or(in("firstName",
                "Sid", "Bob", "John"), eq("age", 90))).
                getExpression().getDescription());
        assertEquals("or(expr=2)", create(or(in("firstName",
                "Sid", "Bob", "John"), eq("age", 90))).
                getExpression().getName());
        assertFalse(in("firstName", "Sid", "Bob", "John").equals(eq("age", 90)));
    }

    @Test
    void twoComparisonsWithNot() {
        ModelFilter<Person> modelFilter = new ModelFilter<>(metadataService.getMetadata(Person.class),
                persons, create(not(contains("firstName", "Jo"))));
        List<Person> result = modelFilter.toList();
        assertEquals(persons.get(1), result.iterator().next());
        assertNotNull(modelFilter.toString());
        assertEquals("!(firstName contains 'Jo')",create(not(contains("firstName",
                "Jo"))).getExpression().getDescription());
        assertEquals("not(expr=1)",create(not(contains("firstName",
                "Jo"))).getExpression().getName());
        assertTrue(not(contains("firstName", "Jo")).equals(not(contains("firstName", "Jo"))));
    }

    private Person createPerson(int age, String description, String firstName, String lastName) {
        Person person = new Person();
        person.setAge(age);
        person.setDescription(description);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        return person;
    }

    private void createPersons() {
        Person person1 = createPerson(10, "I am a 10 year old kid", "John", "Lock");
        Person person2 = createPerson(90, "I am a 90 year old man", "Jack", "Smith");
        Person person3 = createPerson(5, "I am a 5 year old kid", "John", "Rogers");
        persons = List.of(person1, person2, person3);
    }
}