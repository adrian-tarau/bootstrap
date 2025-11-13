package net.microfalx.bootstrap.web.event;

import net.microfalx.lang.annotation.Provider;

@Provider
public class PingEventListener implements EventListener<PingEvent> {

    @Override
    public void onEvent(PingEvent event) {
        // do nothing
    }
}
