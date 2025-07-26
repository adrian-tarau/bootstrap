package net.microfalx.bootstrap.model;

public class DefaultParameters extends AbstractAttributes<Parameter> implements Parameters {

    public DefaultParameters() {
    }

    @Override
    protected DefaultParameter createAttribute(String name, Object value) {
        return new DefaultParameter(name, value);
    }
}
