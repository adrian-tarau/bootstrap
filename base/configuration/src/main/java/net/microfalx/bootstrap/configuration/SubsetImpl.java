package net.microfalx.bootstrap.configuration;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.StringUtils;

import static net.microfalx.bootstrap.configuration.ConfigurationUtils.SEPARATOR;
import static net.microfalx.lang.StringUtils.*;

@Getter
@ToString
class SubsetImpl extends AbstractConfiguration implements Subset {

    private final String prefix;
    private final String title;

    SubsetImpl(ConfigurationService configurationService, Configuration parent, String prefix, String title) {
        super(configurationService, parent);
        this.prefix = defaultIfNull(prefix, EMPTY_STRING);
        this.title = title;
    }

    @Override
    public Subset at(String prefix) {
        if (isEmpty(prefix)) return this;
        String newPrefix = StringUtils.isNotEmpty(this.prefix) ? this.prefix + SEPARATOR + prefix : prefix;
        return new SubsetImpl(getConfigurationService(), this, newPrefix, title);
    }

    @Override
    protected String getFinalKey(String key) {
        if (isNotEmpty(prefix)) {
            return prefix + SEPARATOR + key;
        } else {
            return key;
        }
    }
}
