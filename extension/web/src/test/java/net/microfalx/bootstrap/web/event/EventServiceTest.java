package net.microfalx.bootstrap.web.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.web.application.Application;
import net.microfalx.lang.annotation.Name;
import net.microfalx.lang.annotation.Provider;
import net.microfalx.threadpool.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ThreadPool threadPool;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void before() throws Exception {
        eventService.afterPropertiesSet();
    }

    @Test
    void pingFromClient() {
        eventService.receive(new PingEvent());
    }

    @Test
    void pingFromServer() {
        assertNull(eventService.poll(Application.current()));
        eventService.publish(new PingEvent());
        assertNotNull(eventService.poll(Application.current()));
    }

    @Test
    void decodeEvent() {
        TestEvent event = eventService.decodeEvent("""
                {
                    id:"1",
                    name:"test",
                    arguments:["d1",10]
                }""");
        assertNotNull(event.getId());
        assertEquals("test", event.getName());
        assertEquals("na", event.getApplication());
        assertEquals("d1", event.getData());
        assertEquals(10, event.getIndex());
    }

    @Test
    void listeners() {
        assertEquals(1, eventService.getEventListeners().size());
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @Name("test")
    public static class TestEvent extends AbstractEvent {

        private String data;
        private int index;
    }

    @Provider
    public static class TestEventListener implements EventListener<TestEvent> {

        @Override
        public void onEvent(TestEvent event) {
            // No-op
        }
    }

}