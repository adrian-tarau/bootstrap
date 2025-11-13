package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

import static net.microfalx.lang.StringUtils.defaultIfEmpty;

/**
 * A class which holds information about current (web) application.
 */
@Getter
@ToString
public final class Application implements Nameable, Descriptable {

    String name;
    String description;
    String owner;
    String url;
    String version;
    String logo;

    Theme theme;
    Theme systemTheme;

    /**
     * Returns the current application identifier.
     *
     * @return a non-null instance
     */
    public static String current() {
        return defaultIfEmpty(ApplicationService.APPLICATION.get(), "na");
    }

}
