package net.microfalx.bootstrap.test;

import org.instancio.junit.Given;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstancioTest extends ServiceUnitTestCase {

    @Given
    private Person person;

    @Test
    void extension() {
        assertNotNull(person);
    }

}
