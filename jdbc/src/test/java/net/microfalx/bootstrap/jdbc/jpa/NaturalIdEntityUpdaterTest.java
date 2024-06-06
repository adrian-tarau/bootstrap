package net.microfalx.bootstrap.jdbc.jpa;

import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.model.Metadata;
import net.microfalx.bootstrap.model.MetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class NaturalIdEntityUpdaterTest extends AbstractJpaTestCase {

    @Mock
    private MetadataService metadataService;

    @Autowired
    private TestEntityRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    private NaturalIdEntityUpdater<TestEntity, Integer> entityUpdater;

    @BeforeEach
    void setup() {
        entityUpdater = new NaturalIdEntityUpdater<>(metadataService, repository);
        entityUpdater.setApplicationContext(applicationContext);
        Metadata<TestEntity, Field<TestEntity>, Integer> metadata = Metadata.create(TestEntity.class);
        doReturn(metadata).when(metadataService).getMetadata(any(Object.class));
        doReturn(metadata).when(metadataService).getMetadata(any(Class.class));
    }

    @Test
    void findByNaturalIdOrCreate() {
        TestEntity testEntity = createTestEntity();
        entityUpdater.findByNaturalIdOrCreate(testEntity);
        assertEquals(1, testEntity.getId());
        assertNotNull(testEntity.getCreatedAt());
        assertNull(testEntity.getModifiedAt());
    }

    @Test
    void findByNaturalIdAndUpdate() {
        TestEntity testEntity = createTestEntity();
        entityUpdater.findByNaturalIdOrCreate(testEntity);
        testEntity = testEntity.copy();
        testEntity.setName("Test 1 Test");
        entityUpdater.findByNaturalIdAndUpdate(testEntity);
        assertEquals(1, testEntity.getId());
        assertNotNull(testEntity.getCreatedAt());
        assertNotNull(testEntity.getModifiedAt());
    }

    private TestEntity createTestEntity() {
        TestEntity testEntity = new TestEntity();
        testEntity.setNaturalId("test1");
        testEntity.setName("Test 1");
        return testEntity;
    }

}