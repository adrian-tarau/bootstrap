package net.microfalx.bootstrap.model;

class DefaultAttributes extends AbstractAttributes<DefaultAttribute> {

    @Override
    protected DefaultAttribute createAttribute(String name, Object value) {
        return new DefaultAttribute(name, value);
    }
}
