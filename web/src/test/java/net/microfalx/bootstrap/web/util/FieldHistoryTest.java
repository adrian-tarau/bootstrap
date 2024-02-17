package net.microfalx.bootstrap.web.util;

import net.microfalx.bootstrap.web.preference.PreferenceService;
import net.microfalx.bootstrap.web.preference.PreferenceStorage;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldHistoryTest {

    private final Map<String, byte[]> values = new HashMap<>();

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PreferenceService preferenceService;

    private FieldHistory fieldHistory;

    @BeforeEach
    void setup() {
        values.clear();
        when(authentication.getPrincipal()).thenReturn("user1");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        preferenceService.setStorage(new PreferenceStorageImpl());
        fieldHistory = new FieldHistory(preferenceService, "dataset", "search");
    }

    @Test
    void addSame() {
        fieldHistory.add("field ~ demo");
        fieldHistory.add("field ~ demo");
        Assertions.assertThat(fieldHistory.get()).containsExactly("field ~ demo");
    }

    @Test
    void addMix() {
        fieldHistory.add("field ~ demo1");
        fieldHistory.add("field ~ demo2");
        fieldHistory.add("field ~ demo1");
        Assertions.assertThat(fieldHistory.get()).containsExactly("field ~ demo1", "field ~ demo2");
    }

    @Test
    void addMaxed() {
        for (int i = 0; i < 50; i++) {
            fieldHistory.add("field ~ demo" + i);
        }
        Assertions.assertThat(fieldHistory.get()).hasSize(fieldHistory.getLength());
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

}