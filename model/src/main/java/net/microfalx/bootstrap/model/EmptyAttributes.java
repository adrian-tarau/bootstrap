package net.microfalx.bootstrap.model;

class EmptyAttributes extends AbstractAttributes<DefaultAttributes.DefaultAttribute> {

    EmptyAttributes() {
        setReadOnly(true);
    }

    @Override
    protected DefaultAttributes.DefaultAttribute createAttribute(String name, Object value) {
        return new DefaultAttributes.DefaultAttribute(name, value);
    }
}
