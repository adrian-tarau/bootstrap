package net.microfalx.bootstrap.support.misc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.Alert;
import net.microfalx.bootstrap.dataset.annotation.Filterable;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.dataset.model.Formatters;
import net.microfalx.bootstrap.dataset.model.NamedIdentityAware;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.annotation.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Name("Libraries")
@ReadOnly
@Visible(value = false, fieldNames = "description")
public class Library extends NamedIdentityAware<String> {

    @Position(20)
    @Label(value = "Vendor", group = "Implementation")
    @Description("The vendor which provides the library or specification (API)")
    @Width("140px")
    private String vendor;

    @Position(21)
    @Label(value = "Version", group = "Implementation")
    @Description("The version of the library implementation or specification (API)")
    @Width("75px")
    @Filterable
    private String version;

    @Position(22)
    @Label(value = "Build", group = "Implementation")
    @Description("The build number/commit hash of the library implementation or specification (API)")
    @Width("85px")
    @Filterable
    private String build;

    @Position(22)
    @Label(value = "No", group = "Build")
    @Description("The build number (commit hash) of the library")
    @Width("70px")
    @Filterable
    private String buildNumber;

    @Position(30)
    @Label(value = "Time", group = "Build")
    @Description("The build time of the library")
    @Width("120px")
    @Filterable
    private String buildTime;

    @Position(60)
    @Label(value = "Name", group = "File")
    @Description("The file name of the library")
    @Width("220px")
    @Filterable
    private String fileName;

    @Position(61)
    @Description("The size of the file")
    @Label(value = "Size", group = "File")
    @Formattable(unit = Formattable.Unit.BYTES)
    @Width("60px")
    private long fileSize;

    @Position(62)
    @Label(value = "Modified At", group = "File")
    @Description("The modification time of the library")
    @Formattable(tooltip = Formatters.LastAccessedTooltip.class, elapsed = true)
    @Width("110px")
    private LocalDateTime fileLastModified;

    @Position(63)
    @Label(value = "Info")
    @Description("The additional attributes/information available for the library")
    @Width("50px")
    @Formattable(alert = InfoProvider.class)
    private String info;

    public static class InfoProvider implements Formattable.AlertProvider<Library, Field<Library>, String> {

        @Override
        public Alert provide(String value, Field<Library> field, Library model) {
            Alert.Type type = Alert.Type.INFO;
            return Alert.builder().type(type).icon(Alert.Icon.INFORMATION)
                    .message(model.getDescription()).build();
        }
    }
}
