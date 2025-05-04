package net.microfalx.bootstrap.serenity.annotation;

import net.microfalx.bootstrap.serenity.junit.BoostrapExtension;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * An annotation which enables various internal services.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith({BoostrapExtension.class, SerenityJUnit5Extension.class})
public @interface BootstrapSystemTest {
}
