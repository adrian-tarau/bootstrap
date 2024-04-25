package net.microfalx.bootstrap.dataset;

import jakarta.persistence.*;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSetServiceTest {

    @Mock
    private JpaRepository<Person, Integer> jpaRepository;

    @Spy
    private MetadataService metadataService;

    @InjectMocks
    private DataSetService dataSetService;

    @BeforeEach
    void before() throws Exception {
        metadataService.afterPropertiesSet();
        dataSetService.afterPropertiesSet();
    }

    @Test
    void loadFactories() {
        assertEquals(3, dataSetService.getFactories().size());
    }

    @Test
    void createJpaDataSet() {
        DataSet<Person, Field<Person>, Integer> dataSet = dataSetService.getDataSet(Person.class, jpaRepository);
        assertNotNull(dataSet);
        assertSame(dataSet.getClass(), JpaDataSet.class);
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(new Person()));
        assertEquals(1, dataSet.findAll().size());
    }

    @Test
    void createCustomDataSet() {
        DataSet<TestDataSetFactory.TestModel, Field<TestDataSetFactory.TestModel>, Integer> dataSet = dataSetService.getDataSet(TestDataSetFactory.TestModel.class);
        assertNotNull(dataSet);
        assertSame(dataSet.getClass(), TestDataSetFactory.TestDataSet.class);
    }

    @Entity
    @Table(name = "person")
    public static class Person {

        @Id
        private String id;

        @Column(name = "first_name")
        private String firstName;

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

}