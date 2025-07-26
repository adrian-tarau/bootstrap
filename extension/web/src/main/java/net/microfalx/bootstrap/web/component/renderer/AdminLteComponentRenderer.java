package net.microfalx.bootstrap.web.component.renderer;

import net.microfalx.lang.annotation.Provider;

@Provider
public class AdminLteComponentRenderer extends BootstrapComponentRenderer {

    @Override
    public String getId() {
        return "adminlte";
    }

    @Override
    public String getName() {
        return "AdminLTE";
    }
}
