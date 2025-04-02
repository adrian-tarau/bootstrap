package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.model.MetadataService;
import org.joor.Reflect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractDataSetTestCase {

    private final Validator validator = new TestValidator();

    protected I18nService i18nService = new I18nService();

    protected MetadataService metadataService = new MetadataService();

    @InjectMocks
    protected DataSetService dataSetService;

    @BeforeEach
    void before() throws Exception {
        i18nService.afterPropertiesSet();
        Reflect.on(metadataService).set("i18nService", i18nService);
        Reflect.on(metadataService).set("validator", validator);
        metadataService.afterPropertiesSet();
        Reflect.on(dataSetService).set("i18nService", i18nService);
        Reflect.on(dataSetService).set("metadataService", metadataService);
        dataSetService.afterPropertiesSet();
    }

    private static class TestValidator implements Validator {

        @Override
        public boolean supports(Class<?> clazz) {
            return true;
        }

        @Override
        public void validate(Object target, Errors errors) {
            // all good
        }
    }
}
