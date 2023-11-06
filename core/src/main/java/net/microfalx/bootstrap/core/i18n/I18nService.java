package net.microfalx.bootstrap.core.i18n;

import net.microfalx.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

@Service
public class I18nService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(I18nService.class);

    private static final String SAFE_TEXT_PREFIX = "I18N";

    @Autowired(required = false)
    private MessageSource defaultMessageSource;

    @Autowired(required = false)
    private I18nProperties i18nProperties = new I18nProperties();

    private volatile MessageSource aggregatedMessageSource;
    private final Set<String> baseNames = new HashSet<>();

    /**
     * Returns the aggregated message source.
     * <p>
     * The message source aggregates all the application base names (including i18n) and those dynamically registered on startup
     * and the optional message source provided by Spring Boot.
     *
     * @return a non-null instance
     */
    public MessageSource getMessageSource() {
        if (aggregatedMessageSource == null) buildMessageSource();
        return aggregatedMessageSource;
    }

    /**
     * Returns the value associated with a key
     *
     * @param key the key
     * @return the value, null if not defined
     */
    public String getText(String key) {
        return getText(key, true);
    }

    /**
     * Returns the value associated with a key
     *
     * @param key      the key
     * @param safeText {@code true} to return a safe text when the I18n is missing, {@code false} otherwise
     * @return the value, null if not defined
     */
    public String getText(String key, boolean safeText) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return aggregatedMessageSource.getMessage(key, ObjectUtils.EMPTY_ARRAY, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.debug("Missing i18n '" + key + "' and locale " + locale);
            return getSafeText(key, safeText);
        }
    }

    /**
     * Returns the value associated with a key and format the message using given arguments
     *
     * @param key       the key
     * @param arguments the arguments passed to the message formatter
     * @return the value, null if not defined
     */
    public String getText(String key, Object... arguments) {
        return getText(key, true, arguments);
    }

    /**
     * Returns the value associated with a key and format the message using given arguments
     *
     * @param key       the key
     * @param safeText  {@code true} to return a safe text when the I18n is missing, {@code false} otherwise
     * @param arguments the arguments passed to the message formatter
     * @return the value, null if not defined
     */
    public String getText(String key, boolean safeText, Object... arguments) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return aggregatedMessageSource.getMessage(key, arguments, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.debug("Missing i18n '" + key + "' and locale " + locale);
            return getSafeText(key, safeText);
        }
    }

    /**
     * Registers a new base name.
     *
     * @param name the name
     */
    public void registerBaseName(String name) {
        requireNotEmpty(name);
        baseNames.add(name);
        buildMessageSource();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        buildMessageSource();
    }

    private String getSafeText(String key, boolean safeText) {
        if (safeText) {
            return SAFE_TEXT_PREFIX + "(" + key + ")";
        } else {
            return null;
        }
    }

    private void buildMessageSource() {
        I18nMessageSource messageSource = new I18nMessageSource();
        messageSource.registerMessageSource(internalMessageSource());
        if (defaultMessageSource != null) messageSource.registerMessageSource(defaultMessageSource);
        aggregatedMessageSource = messageSource;
    }

    private ResourceBundleMessageSource internalMessageSource() {
        Set<String> baseNames = new LinkedHashSet<>();
        baseNames.add("i18n");
        baseNames.addAll(i18nProperties.getBaseNames());
        baseNames.addAll(this.baseNames);
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(baseNames.toArray(new String[0]));
        return messageSource;
    }
}
