package net.microfalx.bootstrap.test.extension;

import net.microfalx.bootstrap.configuration.ConfigurationService;
import net.microfalx.bootstrap.core.i18n.I18nService;
import net.microfalx.bootstrap.feature.FeatureService;
import net.microfalx.bootstrap.registry.RegistryService;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.store.StoreService;

@SuppressWarnings("unused")
public class CoreServicesDiscovery implements ComponentDiscovery {

    @Override
    public Class<?>[] value() {
        return new Class[]{I18nService.class, FeatureService.class,
                ResourceService.class, StoreService.class, RegistryService.class, ConfigurationService.class};
    }
}
