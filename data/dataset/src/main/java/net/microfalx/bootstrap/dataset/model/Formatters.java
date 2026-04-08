package net.microfalx.bootstrap.dataset.model;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.FormatterUtils;
import net.microfalx.lang.Ownable;

import static net.microfalx.lang.FormatterUtils.formatElapsed;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.capitalizeWords;

/**
 * A collection of various formatters.
 */
public class Formatters {

    public static class StartedAtTooltip<T> extends TimestampleTooltip<T> {

        @Override
        protected TimestampType getTimestampType() {
            return TimestampType.STARTED;
        }
    }

    public static class CreatedAtTooltip<T> extends TimestampleTooltip<T> {


        @Override
        protected TimestampType getTimestampType() {
            return TimestampType.CREATED;
        }
    }

    public static class FirstAccessedTooltip<T> extends TimestampleTooltip<T> {


        @Override
        protected TimestampType getTimestampType() {
            return TimestampType.FIRST_ACCESSED;
        }
    }

    public static class LastAccessedTooltip<T> extends TimestampleTooltip<T> {


        @Override
        protected TimestampType getTimestampType() {
            return TimestampType.LAST_ACCESSED;
        }
    }

    public static class ModifiedAtTooltip<T> extends TimestampleTooltip<T> {

        @Override
        protected TimestampType getTimestampType() {
            return TimestampType.MODIFIED;
        }
    }

    private static boolean isElapsed(Field<?> field) {
        Formattable formattableAnnot = field.findAnnotation(Formattable.class);
        return formattableAnnot != null ? formattableAnnot.elapsed() : false;
    }

    private static abstract class TimestampleTooltip<T> implements Formattable.TooltipProvider<T, Field<T>, T> {

        private String formatOwner(Ownable<?> ownable) {
            Object owner = isCreated() ? ownable.getCreatedBy() : ownable.getModifiedBy();
            if (owner == null) {
                return EMPTY_STRING;
            } else {
                String text = " by <i>";
                text += isCreated() ? ownable.getCreatedBy() : ownable.getModifiedBy();
                text += "</i>";
                return text;
            }
        }

        private String formatTime(T value) {
            String text = " <b>" + formatElapsed(value, null, true) + "</b>";
            text += " <i>(" + FormatterUtils.formatDateTime(value) + ")</i>";
            return text;
        }

        protected final boolean isCreated() {
            return getTimestampType() == TimestampType.CREATED;
        }

        protected abstract TimestampType getTimestampType();

        protected String getTimestampTypeName() {
            return switch (getTimestampType()) {
                case CREATED -> "Created";
                case STARTED -> "Started";
                case MODIFIED -> "Modified";
                case FIRST_ACCESSED -> "First Accessed";
                case LAST_ACCESSED -> "Last Accessed";
                default -> capitalizeWords(getTimestampType().name());
            };
        }

        @Override
        public final String provide(T value, Field<T> field, T model) {
            String text = getTimestampTypeName();
            text += " " + formatTime(value);
            if (model instanceof Ownable<?> ownable) text += formatOwner(ownable);
            return text;
        }
    }

    enum TimestampType {
        CREATED,
        MODIFIED,
        STARTED,
        FIRST_ACCESSED,
        LAST_ACCESSED,
    }


}
