package net.microfalx.bootstrap.jdbc.entity;

import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.Ownable;

import static net.microfalx.lang.FormatterUtils.formatElapsed;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;

/**
 * A class containing various formatters for base entities.
 */
class EntityFormatters {

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

        protected abstract boolean isCreated();

        @Override
        public final String provide(T value, Field<T> field, T model) {
            String text = isCreated() ? "Created" : "Modified";
            text += " <b>" + formatElapsed(value, null, true) + "</b>";
            if (model instanceof Ownable<?> ownable) text += formatOwner(ownable);
            return text;
        }
    }

    static class CreatedAtTooltip<T> extends TimestampleTooltip<T> {

        @Override
        protected boolean isCreated() {
            return true;
        }

    }

    static class ModifiedAtTooltip<T> extends TimestampleTooltip<T> {

        @Override
        protected boolean isCreated() {
            return false;
        }

    }

}
