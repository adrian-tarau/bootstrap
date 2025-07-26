package net.microfalx.bootstrap.web.component.renderer;

import net.microfalx.lang.annotation.Provider;

@Provider
public class AdminatorComponentRenderer extends BootstrapComponentRenderer {

    @Override
    public String getId() {
        return "adminator";
    }

    @Override
    public String getName() {
        return "Adminator";
    }
}
