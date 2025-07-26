package net.microfalx.bootstrap.web.component.renderer;

import net.microfalx.bootstrap.web.component.Component;
import net.microfalx.bootstrap.web.component.panel.Column;
import net.microfalx.lang.annotation.Provider;

/**
 * Base rules for Bootstrap templates.
 */
@Provider
public class BootstrapComponentRenderer extends ComponentRenderer {

    @Override
    public String getId() {
        return "bootstrap";
    }

    @Override
    public String getName() {
        return "Bootstrap";
    }

    @Override
    public <C extends Component<C>> String doGetCssClass(Component<C> component) {
        if (component instanceof Column) {
            return "col-md-" + ((Column) component).getSpan();
        } else {
            return super.doGetCssClass(component);
        }
    }
}
