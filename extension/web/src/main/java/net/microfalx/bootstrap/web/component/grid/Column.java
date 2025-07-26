package net.microfalx.bootstrap.web.component.grid;

import net.microfalx.bootstrap.web.component.Component;

/**
 * Holds a column in a grid.
 */
public class Column extends Component<Column> {

    private String text;
    private int index;

    public String getText() {
        return text;
    }

    public Column setText(String text) {
        this.text = text;
        return this;
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }
}
