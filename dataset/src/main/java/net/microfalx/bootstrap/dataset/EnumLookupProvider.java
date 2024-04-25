package net.microfalx.bootstrap.dataset;

import java.util.Arrays;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

public class EnumLookupProvider<E extends Enum<E>> extends AbstractLookupProvider<StringLookup, String> {

    private final Class<E> enumClass;

    public EnumLookupProvider(Class<E> enumClass) {
        super(StringLookup.class);
        requireNonNull(enumClass);
        this.enumClass = enumClass;
    }

    @Override
    public Iterable<StringLookup> doFindAll() {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> new StringLookup(e.name(), getDataSetService().getName(e)))
                .toList();
    }
}
