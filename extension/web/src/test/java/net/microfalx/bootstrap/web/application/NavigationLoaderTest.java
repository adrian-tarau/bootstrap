package net.microfalx.bootstrap.web.application;

import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.bootstrap.web.component.Item;
import net.microfalx.bootstrap.web.component.Menu;
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
        Menu menu = applicationService.getNavigation("primary");
        assertEquals("primary", menu.getId());
        assertEquals(2, menu.getChildren().size());

        Iterator<Component<?>> iterator = menu.getChildren().iterator();
        Item item = (Item) iterator.next();
        assertEquals("Home", item.getText());
        assertEquals("/", item.getAction());
        assertEquals(1, item.getPosition());

        Menu parent = (Menu) iterator.next();
        assertEquals("Parent 1", parent.getText());
        assertEquals("/module1", parent.getAction());
        assertEquals(100, parent.getPosition());
        assertEquals(3, parent.getChildren().size());

        iterator = parent.getChildren().iterator();
        item = (Item) iterator.next();
        assertEquals("Child 1", item.getText());
        assertEquals("/module1/child1", item.getAction());
        assertEquals(1, item.getPosition());
        Assertions.assertThat(item.getRoles()).containsExactly("ADMIN");

        item = (Item) iterator.next();
        assertEquals("Child 2", item.getText());
        assertEquals("/module1/child2", item.getAction());
        Assertions.assertThat(item.getRoles()).containsExactlyInAnyOrder("USER", "MANAGER");
    }

    @Test
    void loadTop() {
        loader.load();
        Menu menu = applicationService.getNavigation("top");
        assertEquals("top", menu.getId());
        assertEquals(1, menu.getChildren().size());

        Iterator<Component<?>> iterator = menu.getChildren().iterator();
        Item item = (Item) iterator.next();
        assertEquals("Guest", item.getText());
        assertEquals(null, item.getAction());
        assertEquals(1000, item.getPosition());
        assertEquals("fa-solid fa-user-crown", item.getIcon());
    }

}