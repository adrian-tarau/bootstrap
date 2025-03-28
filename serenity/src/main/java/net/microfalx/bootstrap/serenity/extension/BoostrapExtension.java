package net.microfalx.bootstrap.serenity.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * An extension which adds a few improvements to the Serenity tests.
 */
public class BoostrapExtension implements TestInstancePostProcessor, AfterEachCallback, BeforeEachCallback {

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        System.out.println(SerenityReport.current().generate());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {

    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

    }
}
