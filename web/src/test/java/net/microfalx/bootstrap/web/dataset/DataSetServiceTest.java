package net.microfalx.bootstrap.web.dataset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DataSetServiceTest {

    @InjectMocks
    private DataSetService dataSetService;

    @BeforeEach
    void before() {
        dataSetService.initialize();
    }

    @Test
    void loadFactories() {
        assertEquals(2, dataSetService.getFactories().size());
    }

    @Test
    void createDataSet() {
        DataSet<TestDataSetFactory.TestModel, Integer> dataSet = dataSetService.lookup(TestDataSetFactory.TestModel.class);
        assertNotNull(dataSet);
    }

}