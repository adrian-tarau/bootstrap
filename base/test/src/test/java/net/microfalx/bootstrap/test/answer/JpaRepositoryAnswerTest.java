package net.microfalx.bootstrap.test.answer;

import net.microfalx.bootstrap.test.PersonRepository;
import net.microfalx.bootstrap.test.ServiceUnitTestCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JpaRepositoryAnswerTest extends ServiceUnitTestCase {

    @Mock
    private PersonRepository repository;

    @Test
    void initialize() {
        assertNotNull(repository);
    }

}