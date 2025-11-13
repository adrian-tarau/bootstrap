package net.microfalx.bootstrap.web.event;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

/**
 * An event.
 * <p>
 * The name of the event is given by the {@link net.microfalx.lang.annotation.Name} annotation. In the absence of
 * such annotation, the simple class name is used as the event name by removing the suffix "Event".
 * <p>
 * The event parameters con be defined as members of the class event implementing this interface. The
 * parameters are passed to and from the client in the same order they are defined in the class.
 * <p>
 * Subclasses should extend {@link AbstractEvent} instead of implementing this interface directly and provide a no argument
 * constructor.
 * <p>
 * Each instance of the event is associated with the current application identifier.
 */
public interface Event extends Identifiable<String>, Nameable {

    /**
     * Returns the application identifier the event is associated with.
     *
     * @return a non-null instance
     */
    String getApplication();
}
