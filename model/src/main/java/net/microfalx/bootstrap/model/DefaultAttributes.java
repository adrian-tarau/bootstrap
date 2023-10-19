package net.microfalx.bootstrap.model;

class DefaultAttributes extends AbstractAttributes<DefaultAttributes.DefaultAttribute> {

    @Override
    protected DefaultAttribute createAttribute(String name, Object value) {
        return new DefaultAttribute(name, value);
    }

    static class DefaultAttribute extends AbstractAttribute {

        DefaultAttribute(String name, Object value) {
            super(name, value);
        }
    }
}
