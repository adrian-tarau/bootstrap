package net.microfalx.bootstrap.configuration;

import lombok.Getter;
import lombok.ToString;

import static net.microfalx.bootstrap.configuration.ConfigurationUtils.SEPARATOR;
import static net.microfalx.lang.StringUtils.isEmpty;

@Getter
@ToString
class SubsetImpl extends AbstractConfiguration implements Subset {

    private final String prefix;
    private final String title;

    SubsetImpl(ConfigurationService configurationService, Configuration parent, String prefix, String title) {
        super(configurationService, parent);
        this.prefix = prefix;
        this.title = title;
    }

    @Override
    public Subset at(String prefix) {
        if (isEmpty(prefix)) return this;
        String newPrefix = prefix + SEPARATOR + prefix;
        return new SubsetImpl(getConfigurationService(), this, newPrefix, title);
    }

    @Override
    protected String getFinalKey(String key) {
        return prefix + SEPARATOR + key;
    }
}
