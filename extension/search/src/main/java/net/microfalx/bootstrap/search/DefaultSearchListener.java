package net.microfalx.bootstrap.search;

import net.microfalx.bootstrap.model.AttributeUtils;
import net.microfalx.lang.annotation.Provider;

@Provider
public class DefaultSearchListener implements SearchListener {

    @Override
    public boolean accept(Document document, Attribute attribute) {
        return AttributeUtils.shouldDisplayAsBadge(attribute, false);
    }
}
