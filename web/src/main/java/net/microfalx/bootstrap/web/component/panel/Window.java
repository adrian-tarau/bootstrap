package net.microfalx.bootstrap.web.component.panel;

public class Window extends BasePanel<Window> {

    private boolean modal;

    public boolean isModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
    }
}
