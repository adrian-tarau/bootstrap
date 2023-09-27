package net.microfalx.bootstrap.model;

/**
 * Constants related to attributes.
 */
public class AttributeConstants {

    /**
     * We cannot show too much in the UI when it comes to attributes for a model, so stick with this default number
     */
    public static final int DEFAULT_MAXIMUM_ATTRIBUTES = 15;

    /**
     * We cannot show long attribute, those are probably something that need to be displayed in the details view
     */
    public static final int MAX_ATTRIBUTE_DISPLAY_LENGTH = 50;

    private AttributeConstants() {
    }
}
