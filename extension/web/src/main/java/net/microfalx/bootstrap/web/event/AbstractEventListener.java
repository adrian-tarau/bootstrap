package net.microfalx.bootstrap.web.event;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;

/**
 * Base implementation of an event listener, which is also aware of the application context.
 */
public abstract class AbstractEventListener extends ApplicationContextSupport implements EventListener {

}
