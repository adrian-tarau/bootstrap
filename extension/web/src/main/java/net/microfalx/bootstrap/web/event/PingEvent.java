package net.microfalx.bootstrap.web.event;

import net.microfalx.lang.annotation.Name;

/**
 * A heartbeat event to keep the connection alive.
 */
@Name("ping")
public class PingEvent extends AbstractEvent {

}
