package net.microfalx.bootstrap.web.component.renderer;

import net.microfalx.lang.annotation.Provider;

@Provider
public class EmptyComponentRenderer extends ComponentRenderer {

    @Override
    public String getId() {
        return "empty";
    }

    @Override
    public String getName() {
        return "Empty";
    }
}
