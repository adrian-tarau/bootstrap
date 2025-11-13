package net.microfalx.bootstrap.web.event;

/**
 * An event listener.
 * <p>
 * The listener is mapped to an event name on the client side
 * using {@link net.microfalx.lang.annotation.Name} annotation.
 * <p>
 * Multiple methods can implement this interface to listen and react to multiple
 * event signatures.
 * <p>
 * Application.fireServerEvent(...) methods are used to trigger events on the client side
 * from the server side.
 *
 * @param <E> the event type
 */
public interface EventListener<E extends Event> {

    /**
     * Invoked when an event matching the criteria is published from the client side.
     *
     * @param event the event
     */
    void onEvent(E event);

}
