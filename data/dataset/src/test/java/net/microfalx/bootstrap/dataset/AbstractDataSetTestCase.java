package net.microfalx.bootstrap.dataset;

import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.model.MetadataService;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import net.microfalx.bootstrap.test.annotation.Subject;

public abstract class AbstractDataSetTestCase extends ServiceUnitTestCase {

    @Subject
    protected I18nService i18nService;

    @Subject
    protected MetadataService metadataService;

    @Subject
    protected DataSetService dataSetService;

}
