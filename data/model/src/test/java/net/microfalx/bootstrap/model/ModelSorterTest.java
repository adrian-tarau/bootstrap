package net.microfalx.bootstrap.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ModelSorterTest {


    @InjectMocks
    private MetadataService metadataService;
    private List<PersonJpa> personList;

    @BeforeEach
    void setUp() {
        createPersons();
        assertNotNull(metadataService);
        metadataService.initialize();
    }

    @Test
    void sortWithNoOrders() {
        Sort orders = Sort.unsorted();
        ModelSorter<PersonJpa> modelSorter = new ModelSorter<>
                (metadataService.getMetadata(PersonJpa.class), personList, orders);
        List<PersonJpa> people = modelSorter.toList();
        assertIterableEquals(personList,people);
    }

    @Test
    void sortByFirstAndLastName() {
        Sort.Order order1 = Sort.Order.create("firstName");
        Sort.Order order2 = Sort.Order.create("lastName");
        Sort orders = Sort.create(order1, order2);
        ModelSorter<PersonJpa> modelSorter = new ModelSorter<>
                (metadataService.getMetadata(PersonJpa.class), personList, orders);
        List<PersonJpa> sortedPersonJpaList = modelSorter.toList();
        assertEquals(personList.get(1).getFirstName(),sortedPersonJpaList.get(0).getFirstName());
        assertEquals(personList.get(1).getLastName(),sortedPersonJpaList.get(0).getLastName());
        assertEquals(personList.get(0).getFirstName(),sortedPersonJpaList.get(1).getFirstName());
        assertEquals(personList.get(0).getLastName(),sortedPersonJpaList.get(1).getLastName());
        assertEquals(personList.get(2).getFirstName(),sortedPersonJpaList.get(2).getFirstName());
        assertEquals(personList.get(2).getLastName(),sortedPersonJpaList.get(2).getLastName());
    }

    @Test
    void sortByAge() {
        Sort.Order order1 = Sort.Order.create("age");
        Sort orders = Sort.create(order1);
        ModelSorter<PersonJpa> modelSorter = new ModelSorter<>
                (metadataService.getMetadata(PersonJpa.class), personList, orders);
        List<PersonJpa> sortedPersonJpaList = modelSorter.toList();
        assertEquals(personList.get(2).getAge(),sortedPersonJpaList.get(0).getAge());
        assertEquals(personList.get(0).getAge(),sortedPersonJpaList.get(1).getAge());
        assertEquals(personList.get(1).getAge(),sortedPersonJpaList.get(2).getAge());
    }

    @Test
    void sortByDescription() {
        Sort.Order order1 = Sort.Order.create("description");
        Sort orders = Sort.create(order1);
        ModelSorter<PersonJpa> modelSorter = new ModelSorter<>
                (metadataService.getMetadata(PersonJpa.class), personList, orders);
        List<PersonJpa> sortedPersonJpaList = modelSorter.toList();
        assertEquals(personList.get(0).getDescription(),sortedPersonJpaList.get(0).getDescription());
        assertEquals(personList.get(2).getDescription(),sortedPersonJpaList.get(1).getDescription());
        assertEquals(personList.get(1).getAge(),sortedPersonJpaList.get(2).getAge());
    }

    private PersonJpa createPersonJPA(int age, String description, String firstName, String lastName) {
        PersonJpa personJpa = new PersonJpa();
        personJpa.setAge(age);
        personJpa.setDescription(description);
        personJpa.setFirstName(firstName);
        personJpa.setLastName(lastName);
        return personJpa;
    }

    private void createPersons() {
        PersonJpa person1 = createPersonJPA(10, "I am a 10 year old kid", "John", "Lock");
        PersonJpa person2 = createPersonJPA(90, "I am a 90 year old man", "Jack", "Smith");
        PersonJpa person3 = createPersonJPA(5, "I am a 5 year old kid", "John", "Rogers");
        personList = List.of(person1, person2, person3);
    }
}