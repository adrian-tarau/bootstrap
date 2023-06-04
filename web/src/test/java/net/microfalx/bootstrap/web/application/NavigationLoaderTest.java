package net.microfalx.bootstrap.web.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NavigationLoaderTest {

    @Mock
    private Application application;

    @InjectMocks
    private ApplicationService applicationService;

    private NavigationLoader loader;

    @BeforeEach
    void setup() {
        loader = new NavigationLoader(applicationService);
    }

    @Test
    void loadLeft() {
        loader.load();
        Navigation navigation = applicationService.getNavigation("left");
        assertEquals("left", navigation.getId());
        assertEquals(2, navigation.getLinks().size());

        Iterator<Link> iterator = navigation.getLinks().iterator();
        Link link = iterator.next();
        assertEquals("Home", link.getName());
        assertEquals("/", link.getTarget());
        assertEquals(1, link.getOrder());
        link = iterator.next();
        assertEquals("Parent 1", link.getName());
        assertEquals("/module1", link.getTarget());
        assertEquals(100, link.getOrder());
        assertEquals(3, link.getLinks().size());

        iterator = link.getLinks().iterator();
        link = iterator.next();
        assertEquals("Child 1", link.getName());
        assertEquals("/module1/child1", link.getTarget());
        assertEquals(1, link.getOrder());
        Assertions.assertThat(link.getRoles()).containsExactly("ADMIN");
        link = iterator.next();
        assertEquals("Child 2", link.getName());
        assertEquals("/module1/child2", link.getTarget());
        Assertions.assertThat(link.getRoles()).containsExactlyInAnyOrder("USER", "MANAGER");
    }

}