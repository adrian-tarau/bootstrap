package net.microfalx.bootstrap.search;

import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.annotation.Provider;

@Provider
public class DefaultSearchListener implements SearchListener {

    private static final int MAX_ATTRIBUTE_DISPLAY_LENGTH = 60;

    @Override
    public boolean accept(Document document, Attribute attribute) {
        if (ObjectUtils.isEmpty(attribute.getValue())) return false;
        if (!(attribute.getValue() instanceof String)) return true;
        String text = (String) attribute.getValue();
        return text.length() < MAX_ATTRIBUTE_DISPLAY_LENGTH && (int) text.lines().count() == 1;
    }
}
