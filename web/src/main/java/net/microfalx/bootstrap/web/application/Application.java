package net.microfalx.bootstrap.web.application;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

public final class Application implements Nameable, Descriptable {

    String name;
    String description;
    String version;

    Theme theme;

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

    public Theme getTheme() {
        return theme;
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", theme=" + theme +
                '}';
    }
}
