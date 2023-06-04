package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

public final class Application implements Nameable, Descriptable {

    String name;
    String description;
    String version;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }
}
