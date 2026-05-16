package net.microfalx.bootstrap.trace.startup;

import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static java.lang.System.currentTimeMillis;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.lang.TimeUtils.toLocalDateTime;

@Getter
@ToString
public class StartupStepImpl implements StartupStep {

    private final String name;
    private final long id;
    private final Long parentId;
    private final long start;
    private volatile long end;

    private String beanType;
    private String beanName;
    private List<Tag> tags;

    static final ThreadLocal<Stack<StartupStepImpl>> PARENTS = ThreadLocal.withInitial(Stack::new);

    StartupStepImpl(String name) {
        this.name = name;
        id = StartupTimeline.ID_GENERATOR.getAndIncrement();
        Stack<StartupStepImpl> steps = PARENTS.get();
        parentId = !steps.isEmpty() ? steps.peek().getId() : null;
        start = currentTimeMillis();
        steps.add(this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public @Nullable Long getParentId() {
        return parentId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LocalDateTime getStartedAt() {
        return toLocalDateTime(start);
    }

    @Override
    public LocalDateTime getEndedAt() {
        if (end == 0) {
            return LocalDateTime.now();
        } else {
            return toLocalDateTime(end);
        }
    }

    @Override
    public Optional<String> getBeanClassName() {
        if (isNotEmpty(beanType)) {
            return Optional.of(beanType);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public StartupStep tag(String key, String value) {
        if ("beanType".equals(key)) {
            this.beanType = normalizeClassName(value);
        } else if ("beanName".equals(key)) {
            this.beanName = value;
        } else {
            addTag(key, value);
        }
        return this;
    }

    @Override
    public StartupStep tag(String key, Supplier<String> value) {
        String supplierValue = naIfEmpty(value.get());
        if ("beanType".equals(key)) {
            this.beanType = normalizeClassName(supplierValue);
        } else if ("beanName".equals(key)) {
            this.beanName = supplierValue;
        } else {
            addTag(key, supplierValue);
        }
        return this;
    }

    @Override
    public Tags getTags() {
        return new TagsImpl();
    }

    @Override
    public void end() {
        end = currentTimeMillis();
        Stack<StartupStepImpl> steps = PARENTS.get();
        if (!steps.isEmpty()) steps.pop();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StartupStepImpl that = (StartupStepImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private void addTag(String key, String value) {
        getOrCreateTags().add(new TagImpl(key, value));
    }

    private List<Tag> getOrCreateTags() {
        if (tags == null) tags = new ArrayList<>(5);
        return tags;
    }

    private String normalizeClassName(String value) {
        value = StringUtils.replaceFirst(value, "class ", EMPTY_STRING);
        value = StringUtils.replaceFirst(value, "interface ", EMPTY_STRING);
        return ClassUtils.getCompactName(value);
    }

    @Getter
    @ToString
    private static final class TagImpl implements Tag {

        private final String key;
        private final String value;

        public TagImpl(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private final class TagsImpl implements Tags {

        @Override
        public @NonNull Iterator<Tag> iterator() {
            return getOrCreateTags().iterator();
        }
    }


}
