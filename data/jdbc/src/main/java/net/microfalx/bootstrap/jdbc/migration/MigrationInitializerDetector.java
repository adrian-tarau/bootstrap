package net.microfalx.bootstrap.jdbc.migration;

import org.springframework.boot.sql.init.dependency.AbstractBeansOfTypeDatabaseInitializerDetector;

import java.util.Collections;
import java.util.Set;

public class MigrationInitializerDetector extends AbstractBeansOfTypeDatabaseInitializerDetector {

    @Override
    protected Set<Class<?>> getDatabaseInitializerBeanTypes() {
        return Collections.singleton(MigrationInitializer.class);
    }

}
