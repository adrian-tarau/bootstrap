package net.microfalx.bootstrap.web.preference;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.Principal;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * A service which handles user preferences.
 * <p>
 * The service delegates the persistence of
 */
@Service
public class PreferenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceService.class);

    /**
     * The user name for a user which is not authenticated
     */
    private static final String ANONYMOUS_USER = "anonymous";

    private volatile PreferenceStorage storage;

    /**
     * Returns a setting for the current user.
     *
     * @param name the setting name
     * @return the setting, null if it does not exist
     */
    public <T> Preference<T> get(String name) {
        return get(name, getCurrentUserName());
    }

    /**
     * Returns a setting for a given user.
     *
     * @param name     the setting name
     * @param userName the user, if null will be the current user
     * @return the setting, null if it does not exist
     */
    public <T> Preference<T> get(String name, String userName) {
        requireNonNull(userName);
        requireNotEmpty(name);
        if (StringUtils.isEmpty(userName)) userName = getCurrentUserName();
        byte[] data = getStorage().load(userName, name);
        T value = decodeValue(data);
        return new Preference<>(name, value);
    }

    /**
     * Changes a setting for the current user.
     *
     * @param value the preference
     */
    public <T> void set(Preference<T> value) {
        set(value, getCurrentUserName());
    }

    /**
     * Changes a setting for a given user.
     *
     * @param value    the preference
     * @param userName the user name
     */
    public <T> void set(Preference<T> value, String userName) {
        requireNotEmpty(userName);
        requireNotEmpty(value);
        LOGGER.debug("Persist preference for user {}, name {}, value {}", userName, value.getName(), value.getValue());
        byte[] data = encodeValue(value.getValue(null));
        getStorage().store(userName, value.getName(), data);
    }

    /**
     * Changes the preference storage.
     *
     * @param storage the storage
     */
    public void setStorage(PreferenceStorage storage) {
        requireNonNull(storage);
        this.storage = storage;
        LOGGER.info("Preference storage changed to '{}'", ClassUtils.getName(storage));
    }

    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            return ((Principal) principal).getName();
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            return ANONYMOUS_USER;
        }
    }

    private byte[] encodeValue(Object value) {
        if (ObjectUtils.isEmpty(value)) return null;
        ObjectMapper mapper = createMapper();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(buffer);
        try {
            writer.append(ClassUtils.getName(value)).append("\n");
            mapper.writeValue(writer, value);
        } catch (Exception e) {
            throw new PreferenceException("Failed to encode value for class '" + ClassUtils.getName(value) + "'", e);
        }
        return buffer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private <T> T decodeValue(byte[] value) {
        if (ObjectUtils.isEmpty(value)) return null;
        ObjectMapper mapper = createMapper();
        Reader reader = new InputStreamReader(new ByteArrayInputStream(value));
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        String className = StringUtils.NA_STRING;
        try {
            className = lineNumberReader.readLine();
            Class<?> valueClass = Class.forName(className);
            return (T) mapper.readValue(lineNumberReader, valueClass);
        } catch (Exception e) {
            throw new PreferenceException("Failed to decode value for class '" + className + "'", e);
        }
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    private PreferenceStorage getStorage() {
        if (storage == null) {
            throw new IllegalStateException("A persistence listener for user preferences is not provided");
        }
        return storage;
    }
}
