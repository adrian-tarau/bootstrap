package net.microfalx.bootstrap.test;

import net.microfalx.bootstrap.configuration.ConfigurationService;
import net.microfalx.bootstrap.registry.Registry;
import net.microfalx.bootstrap.resource.ResourceService;
import net.microfalx.bootstrap.store.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @Autowired
    private Registry registry;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ConfigurationService configurationService;
}
