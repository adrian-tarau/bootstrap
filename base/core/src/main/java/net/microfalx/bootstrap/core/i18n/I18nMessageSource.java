package net.microfalx.bootstrap.core.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A {@link MessageSource} which delegates to a collection of message sources in the order of the registration.
 */
public class I18nMessageSource implements MessageSource {

    private final Collection<MessageSource> sources = new ArrayList<>();

    public void registerMessageSource(MessageSource source) {
        requireNonNull(source);
        this.sources.add(source);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        NoSuchMessageException exception = null;
        for (MessageSource source : sources) {
            try {
                return source.getMessage(code, args, defaultMessage, locale);
            } catch (NoSuchMessageException e) {
                exception = e;
            }
        }
        throw exception;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        NoSuchMessageException exception = null;
        for (MessageSource source : sources) {
            try {
                return source.getMessage(code, args, locale);
            } catch (NoSuchMessageException e) {
                exception = e;
            }
        }
        throw exception;
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        NoSuchMessageException exception = null;
        for (MessageSource source : sources) {
            try {
                return source.getMessage(resolvable, locale);
            } catch (NoSuchMessageException e) {
                exception = e;
            }
        }
        throw exception;
    }
}
