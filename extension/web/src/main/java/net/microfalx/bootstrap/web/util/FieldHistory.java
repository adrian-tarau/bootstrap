package net.microfalx.bootstrap.web.util;

import net.microfalx.bootstrap.web.preference.Preference;
import net.microfalx.bootstrap.web.preference.PreferenceService;
import net.microfalx.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireBounded;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A class which manages the history of a (data set) field, within a category.
 * <p>
 * It uses {@link PreferenceService} to persis the history if a field
 */
public class FieldHistory {

    private final PreferenceService preferenceService;
    private final String owner;
    private final String name;

    private int length = 10;

    public FieldHistory(PreferenceService preferenceService, String owner, String name) {
        requireNonNull(preferenceService);
        requireNonNull(owner);
        requireNonNull(name);
        this.preferenceService = preferenceService;
        this.owner = owner;
        this.name = name;
    }

    /**
     * Returns the controller path where this history applies.
     *
     * @return a non-null instance
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns the field name.
     *
     * @return a non-null instance
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the length of the history.
     *
     * @return a positive integer
     */
    public int getLength() {
        return length;
    }

    /**
     * Changes the length of the history.
     *
     * @param length a positive integer
     * @return self
     */
    public FieldHistory setLength(int length) {
        requireBounded(length, 1, 100);
        this.length = length;
        return this;
    }

    /**
     * Returns the history of the field.
     *
     * @return a non-null instance
     */
    public List<String> get() {
        Preference<History> settings = preferenceService.get(getKey());
        return settings.getValue().isPresent() ? settings.getValue().get().values : new ArrayList<>();
    }

    /**
     * Adds a new value in the history.
     * <p>
     * No duplicates will be added but if the value is already there, it will be promoted at the top
     * of the list.
     * <p>
     * If the value is empty or the value is already at the top of the list, no change is made to the history.
     *
     * @param value the value
     */
    public void add(String value) {
        if (StringUtils.isEmpty(value)) return;
        List<String> values = get();
        if (!values.isEmpty() && values.get(0).equalsIgnoreCase(value)) return;
        values.remove(value);
        values.add(0, value);
        while (values.size() > length) {
            values.remove(values.size() - 1);
        }
        History history = new History(values);
        preferenceService.set(Preference.create(getKey(), history));
    }

    private String getKey() {
        String key = name;
        if (StringUtils.isNotEmpty(owner)) key = owner + ":" + key;
        return StringUtils.toLowerCase(key);
    }

    public static class History {

        private List<String> values = new ArrayList<>();

        public History() {
        }

        public History(List<String> values) {
            this.values = values;
        }

        public List<String> getValues() {
            return values;
        }
    }
}
