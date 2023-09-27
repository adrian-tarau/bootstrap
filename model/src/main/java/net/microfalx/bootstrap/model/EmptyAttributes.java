package net.microfalx.bootstrap.model;

class EmptyAttributes extends AbstractAttributes<DefaultAttribute> {

    EmptyAttributes() {
        setReadOnly(true);
    }

    @Override
    protected DefaultAttribute createAttribute(String name, Object value) {
        return new DefaultAttribute(name, value);
    }
}
