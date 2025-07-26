package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.dataset.model.PersonJpa;
import net.microfalx.bootstrap.model.Field;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DataSetServiceTest extends AbstractDataSetTestCase {

    @Mock
    private JpaRepository<PersonJpa, Integer> jpaRepository;

    @Test
    void loadFactories() {
        assertEquals(5, dataSetService.getFactories().size());
    }

    @Test
    void createJpaDataSet() {
        DataSet<PersonJpa, Field<PersonJpa>, Integer> dataSet = dataSetService.getDataSet(PersonJpa.class, jpaRepository);
        assertNotNull(dataSet);
        assertSame(dataSet.getClass(), JpaDataSet.class);
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(new PersonJpa()));
        assertEquals(1, dataSet.findAll().size());
    }

    @Test
    void createCustomDataSet() {
        DataSet<TestDataSetFactory.TestModel, Field<TestDataSetFactory.TestModel>, Integer> dataSet = dataSetService.getDataSet(TestDataSetFactory.TestModel.class);
        assertNotNull(dataSet);
        assertSame(dataSet.getClass(), TestDataSetFactory.TestDataSet.class);
    }

}