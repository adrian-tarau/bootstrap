package net.microfalx.bootstrap.web.preference;

import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceTest {

    private final Map<String, byte[]> values = new HashMap<>();

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PreferenceService preferenceService;


    @BeforeEach
    void setup() {
        values.clear();
        when(authentication.getPrincipal()).thenReturn("user1");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        preferenceService.setStorage(new PreferenceStorageImpl());
    }

    @Test
    void withNull() {
        assertNull(preferenceService.get("k1").getValue(null));
        preferenceService.set(Preference.create("k1"));
        assertNull(preferenceService.get("k1").getValue(null));
    }

    @Test
    void withString() {
        assertNull(preferenceService.get("s1").getValue(null));
        preferenceService.set(Preference.create("s1", "test"));
        assertEquals("test", preferenceService.get("s1").getValue(null));
    }

    @Test
    void withInteger() {
        assertNull(preferenceService.get("i1").getValue(null));
        preferenceService.set(Preference.create("i1", 10));
        assertEquals(10, preferenceService.get("i1").getValue(null));
    }

    @Test
    void withLong() {
        assertNull(preferenceService.get("l1").getValue(null));
        preferenceService.set(Preference.create("l1", 10L));
        assertEquals(10L, preferenceService.get("l1").getValue(null));
    }

    @Test
    void withFloat() {
        assertNull(preferenceService.get("f1").getValue(null));
        preferenceService.set(Preference.create("f1", 10.5f));
        assertEquals(10.5f, (float) preferenceService.get("f1").getValue(null), 0.001);
    }

    @Test
    void withDouble() {
        assertNull(preferenceService.get("l1").getValue(null));
        preferenceService.set(Preference.create("l1", 10.5d));
        assertEquals(10.5d, preferenceService.get("l1").getValue(null));
    }

    @Test
    void withTemporals() {
        assertNull(preferenceService.get("ld11").getValue(null));
        LocalDate now = LocalDate.now();
        preferenceService.set(Preference.create("ld1", now));
        assertEquals(now, preferenceService.get("ld1").getValue(null));

        assertNull(preferenceService.get("ldt11").getValue(null));
        LocalDateTime nowLt = LocalDateTime.now();
        preferenceService.set(Preference.create("ldt11", nowLt));
        assertEquals(nowLt, preferenceService.get("ldt11").getValue(null));

        assertNull(preferenceService.get("zdt11").getValue(null));
        ZonedDateTime nowZdt = ZonedDateTime.now();
        preferenceService.set(Preference.create("zdt11", nowZdt));
        // why the zone is lost
        //assertEquals(nowZdt, ((ZonedDateTime) preferenceService.get("zdt11").getValue(null)));
    }

    @Test
    void withComplex() {
        ComplexObject2 co2 = new ComplexObject2();
        preferenceService.set(Preference.create("co1", co2));
        ComplexObject2 co2loaded = (ComplexObject2) preferenceService.get("co1").getValue(null);
        Assertions.assertThat(co2loaded).usingRecursiveComparison().isEqualTo(co2);
    }

    class PreferenceStorageImpl implements PreferenceStorage {

        @Override
        public void store(String userName, String name, byte[] value) {
            values.put(getKey(userName, name), value);
        }

        @Override
        public byte[] load(String userName, String name) {
            return values.get(getKey(userName, name));
        }

        private String getKey(String userName, String name) {
            return userName + ":" + name;
        }
    }

    @Getter
    static class ComplexObject1 {

        private int i1 = 1;
        private long l1 = 10;
        private String s1 = "s1";
    }

    @Getter
    static class ComplexObject2 {

        private int i1 = 2;
        private long l1 = 20;
        private String s1 = "s3";
        private ComplexObject1 co1 = new ComplexObject1();
    }

}