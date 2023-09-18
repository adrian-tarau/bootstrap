package net.microfalx.bootstrap.core.i18n;

import net.microfalx.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class I18nService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(I18nService.class);

    @Autowired
    private MessageSource messageSource;

    /**
     * Returns the value associated with a key
     *
     * @param key the key
     * @return the value, null if not defined
     */
    public String getText(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, ObjectUtils.EMPTY_ARRAY, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.debug("Missing i18n '" + key + "' and locale " + locale);
            return null;
        }
    }

    /**
     * Returns the value associated with a key and format the message using given arguments
     *
     * @param key the key
     * @return the value, null if not defined
     */
    public String getText(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.debug("Missing i18n '" + key + "' and locale " + locale);
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
