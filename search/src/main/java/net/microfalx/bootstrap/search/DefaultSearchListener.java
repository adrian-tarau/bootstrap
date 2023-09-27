package net.microfalx.bootstrap.search;

import net.microfalx.lang.annotation.Provider;

import static net.microfalx.bootstrap.model.AttributeConstants.MAX_ATTRIBUTE_DISPLAY_LENGTH;

@Provider
public class DefaultSearchListener implements SearchListener {

    @Override
    public boolean accept(Document document, Attribute attribute) {
        if (attribute.isEmpty()) return false;
        if (!(attribute.getValue() instanceof String)) return true;
        String text = attribute.asString();
        return text.length() < MAX_ATTRIBUTE_DISPLAY_LENGTH && (int) text.lines().count() == 1;
    }
}
