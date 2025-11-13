package net.microfalx.bootstrap.web.event;

import com.fasterxml.jackson.databind.JsonNode;
import net.microfalx.lang.annotation.Name;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Utility class for event related operations.
 */
public class EventUtilities {

    /**
     * Reads the event name from the payload.
     *
     * @param node the JSON node
     * @return the name of the event
     */
    public static String getEventName(JsonNode node) {
        requireNonNull(node);
        JsonNode nameNode = node.get("name");
        if (nameNode == null) throw new EventException("Event name is not set for event " + node.toPrettyString());
        return nameNode.asText();
    }

    /**
     * Extracts the event name from the event class.
     *
     * @param eventClass the event class
     * @return the event name
     */
    public static String getEventName(Class<?> eventClass) {
        requireNonNull(eventClass);
        Name nameAnnot = eventClass.getAnnotation(Name.class);
        if (nameAnnot != null) {
            return nameAnnot.value();
        } else {
            String simpleName = eventClass.getSimpleName();
            if (simpleName.endsWith("Event")) {
                return simpleName.substring(0, simpleName.length() - 4);
            } else {
                return simpleName;
            }
        }
    }

}
