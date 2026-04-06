package net.microfalx.bootstrap.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationUtilsTest {

    @Test
    void getParentWithSimpleKey() {
        String parent = ConfigurationUtils.getParent("system.config");
        assertThat(parent).isEqualTo("system");
    }

    @Test
    void getParentWithMultipleLevels() {
        String parent = ConfigurationUtils.getParent("system.config.database.host");
        assertThat(parent).isEqualTo("system.config.database");
    }

    @Test
    void getParentWithSingleComponent() {
        String parent = ConfigurationUtils.getParent("system");
        assertThat(parent).isEmpty();
    }

    @Test
    void getParentWithEmptyString() {
        String parent = ConfigurationUtils.getParent("");
        assertThat(parent).isEmpty();
    }

    @Test
    void getParentWithNull() {
        String parent = ConfigurationUtils.getParent(null);
        assertThat(parent).isEmpty();
    }

    @Test
    void getParentWithOnlyDot() {
        String parent = ConfigurationUtils.getParent(".");
        assertThat(parent).isEmpty();
    }

    @Test
    void getParentWithLeadingDot() {
        String parent = ConfigurationUtils.getParent(".system.config");
        assertThat(parent).isEqualTo(".system");
    }

    @Test
    void getParentWithTrailingDot() {
        String parent = ConfigurationUtils.getParent("system.config.");
        assertThat(parent).isEqualTo("system.config");
    }

    @Test
    void getLastWithSimpleKey() {
        String last = ConfigurationUtils.getLast("system.config");
        assertThat(last).isEqualTo("config");
    }

    @Test
    void getLastWithMultipleLevels() {
        String last = ConfigurationUtils.getLast("system.config.database.host");
        assertThat(last).isEqualTo("host");
    }

    @Test
    void getLastWithSingleComponent() {
        String last = ConfigurationUtils.getLast("system");
        assertThat(last).isEqualTo("system");
    }

    @Test
    void getLastWithEmptyString() {
        String last = ConfigurationUtils.getLast("");
        assertThat(last).isEmpty();
    }

    @Test
    void getLastWithNull() {
        String last = ConfigurationUtils.getLast(null);
        assertThat(last).isEmpty();
    }

    @Test
    void getLastWithOnlyDot() {
        String last = ConfigurationUtils.getLast(".");
        assertThat(last).isEmpty();
    }

    @Test
    void getLastWithLeadingDot() {
        String last = ConfigurationUtils.getLast(".system");
        assertThat(last).isEqualTo("system");
    }

    @Test
    void getLastWithTrailingDot() {
        String last = ConfigurationUtils.getLast("system.");
        assertThat(last).isEqualTo("system");
    }

    @Test
    void getLastWithMultipleDots() {
        String last = ConfigurationUtils.getLast("system..config");
        assertThat(last).isEqualTo("config");
    }

    @Test
    void getParentAndLastAreComplementary() {
        String key = "system.config.database";
        String parent = ConfigurationUtils.getParent(key);
        String last = ConfigurationUtils.getLast(key);
        if (!parent.isEmpty()) {
            assertThat(parent + "." + last).isEqualTo(key);
        }
    }

    @Test
    void getTitleWithSimpleKey() {
        String title = ConfigurationUtils.getTitle("system.config");
        assertThat(title).isEqualTo("Config");
    }

    @Test
    void getTitleWithCamelCaseKey() {
        String title = ConfigurationUtils.getTitle("system.databaseUrl");
        assertThat(title).isEqualTo("Database Url");
    }

    @Test
    void getTitleWithMultipleLevels() {
        String title = ConfigurationUtils.getTitle("system.config.database.connectionPool");
        assertThat(title).isEqualTo("Connection Pool");
    }

    @Test
    void getTitleWithSingleComponent() {
        String title = ConfigurationUtils.getTitle("database");
        assertThat(title).isEqualTo("Database");
    }

    @Test
    void getTitleWithEmptyString() {
        String title = ConfigurationUtils.getTitle("");
        assertThat(title).isEqualTo("N/A");
    }

    @Test
    void getTitleWithNull() {
        String title = ConfigurationUtils.getTitle(null);
        assertThat(title).isEqualTo("N/A");
    }

    @Test
    void getTitleWithOnlyDot() {
        String title = ConfigurationUtils.getTitle(".");
        assertThat(title).isEqualTo("N/A");
    }

    @Test
    void getTitleWithTrailingDot() {
        String title = ConfigurationUtils.getTitle("system.config.");
        assertThat(title).isEqualTo("Config");
    }

    @Test
    void getTitleWithAllCaps() {
        String title = ConfigurationUtils.getTitle("system.URL");
        assertThat(title).isEqualTo("URL");
    }

    @Test
    void getTitleWithNumbers() {
        String title = ConfigurationUtils.getTitle("system.config2Database");
        assertThat(title).isEqualTo("Config 2Database");
    }

}