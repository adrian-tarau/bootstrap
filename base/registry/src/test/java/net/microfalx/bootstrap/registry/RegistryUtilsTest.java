package net.microfalx.bootstrap.registry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RegistryUtils Tests")
class RegistryUtilsTest {

    @Test
    void getParentWithParent() {
        String result = RegistryUtils.getParent("/parent/child");
        assertEquals("/parent", result);
    }

    @Test
    void getParentSingleLevel() {
        String result = RegistryUtils.getParent("/child");
        assertEquals("/", result);
    }

    @Test
    void getParentEmptyPath() {
        String result = RegistryUtils.getParent("");
        assertEquals("/", result);
    }

    @Test
    void getParentNullPath() {
        String result = RegistryUtils.getParent(null);
        assertEquals("/", result);
    }

    @Test
    void getParentRootPath() {
        String result = RegistryUtils.getParent("/");
        assertEquals("/", result);
    }

    @Test
    void getParentNestedPath() {
        String result = RegistryUtils.getParent("/a/b/c/d");
        assertEquals("/a/b/c", result);
    }

    @Test
    void normalizePathEmpty() {
        String result = RegistryUtils.normalizePath("");
        assertEquals("/", result);
    }

    @Test
    void normalizePathNull() {
        String result = RegistryUtils.normalizePath(null);
        assertEquals("/", result);
    }

    @Test
    void normalizePathRoot() {
        String result = RegistryUtils.normalizePath("/");
        assertEquals("/", result);
    }

    @Test
    void normalizePathSimple() {
        String result = RegistryUtils.normalizePath("/simple");
        assertEquals("/simple", result);
    }

    @Test
    void normalizePathMultiLevel() {
        String result = RegistryUtils.normalizePath("/parent/child");
        assertEquals("/parent/child", result);
    }

    @Test
    void normalizePathConvertToIdentifiers() {
        // Parts with spaces and special characters should be converted to identifiers
        String result = RegistryUtils.normalizePath("/parent name/child-id");
        assertEquals("/parent_name/child_id", result);
    }

    @Test
    void normalizePathMixedCase() {
        String result = RegistryUtils.normalizePath("/Parent/Child");
        assertEquals("/parent/child", result);
    }

    @Test
    void normalizePathTrailingSlash() {
        String result = RegistryUtils.normalizePath("/parent/child/");
        assertEquals("/parent/child", result);
    }

    @Test
    void normalizePathEmptySegments() {
        String result = RegistryUtils.normalizePath("/parent//child");
        assertEquals("/parent/child", result);
    }

    @Test
    void normalizePathComplexIdentifiers() {
        String result = RegistryUtils.normalizePath("/User-Profile/My Data");
        assertEquals("/user_profile/my_data", result);
    }

}