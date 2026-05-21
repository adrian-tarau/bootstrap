package net.microfalx.bootstrap.logger;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import net.microfalx.lang.IdentityAware;
import net.microfalx.lang.NamedIdentityAware;

import java.util.HashSet;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.CollectionUtils.immutableSet;

/**
 * Holds the information about the appender
 */
@Getter
public class Appender extends NamedIdentityAware<String> {

    private String fileName;
    private Set<String> included;
    private Set<String> excluded;

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public Set<String> getIncluded() {
        return immutableSet(included);
    }

    public Set<String> getExcluded() {
        return immutableSet(excluded);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("super", super.toString())
                .add("fileName", fileName)
                .add("included", included)
                .add("excluded", excluded)
                .toString();
    }


    public static final class Builder extends NamedIdentityAware.Builder<String> {

        private String fileName;
        private Set<String> included;
        private Set<String> excluded;

        public Builder(String id) {
            super(id);
            this.fileName = id.toLowerCase() + ".log";
        }

        public Builder fileName(String fileName) {
            requireNotEmpty(fileName);
            this.fileName = fileName;
            return this;
        }

        public Builder included(String included) {
            requireNotEmpty(included);
            if (this.included == null) this.included = new HashSet<>();
            this.included.add(included);
            return this;
        }

        public Builder excluded(String excluded) {
            requireNotEmpty(excluded);
            if (this.excluded == null) this.excluded = new HashSet<>();
            this.excluded.add(excluded);
            return this;
        }

        @Override
        protected IdentityAware<String> create() {
            return new Appender();
        }

        @Override
        public Appender build() {
            Appender appender = (Appender) super.build();
            appender.fileName = fileName;
            appender.included = included;
            appender.excluded = excluded;
            return appender;
        }
    }
}
