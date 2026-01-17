package net.microfalx.bootstrap.web.application;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Nameable;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Application that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(owner, that.owner) && Objects.equals(url, that.url)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner, url, version);
    }

    /**
     * Returns the current application identifier.
     *
     * @return a non-null instance
     */
    public static String current() {
        return defaultIfEmpty(ApplicationService.APPLICATION.get(), "na");
    }

}
