package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

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

}
