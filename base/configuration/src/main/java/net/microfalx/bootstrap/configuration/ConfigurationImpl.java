package net.microfalx.bootstrap.configuration;

class ConfigurationImpl extends AbstractConfiguration {

    ConfigurationImpl(ConfigurationService configurationService) {
        super(configurationService, null);
    }

    @Override
    public Subset at(String prefix) {
        return new SubsetImpl(getConfigurationService(), this, prefix, ConfigurationUtils.getTitle(prefix));
    }
}
