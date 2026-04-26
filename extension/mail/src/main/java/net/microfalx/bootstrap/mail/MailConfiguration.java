package net.microfalx.bootstrap.mail;

import net.microfalx.bootstrap.configuration.ConfigurationListenerAware;
import net.microfalx.bootstrap.configuration.annotation.ConfigurationMapping;
import net.microfalx.lang.annotation.DefaultValue;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * The configuration for Mail service.
 */
@Component
@ConfigurationMapping(prefix = "bootstrap.mail")
public interface MailConfiguration extends ConfigurationListenerAware {

    /**
     * Returns the hostname of the SMTP gateway
     *
     * @return a non-null string
     */
    @DefaultValue("locahost")
    String getHost();

    /**
     * Returns the port of the SMTP gateway.
     *
     * @return a positive integer
     */
    @DefaultValue("25")
    int getPort();

    /**
     * Returns whether the SMTP gateway requires TLS.
     *
     * @return <code>true</code> true if TLS is required, <code>false</code>
     */
    boolean isTls();

    /**
     * Returns the username to authenticate with the SMTP gateway
     *
     * @return the username, null/empty if authentication is not required
     */
    String getUserName();

    /**
     * Returns the password to authenticate with the SMTP gateway
     *
     * @return the password, null/empty if authentication is not required
     */
    String getPassword();

    /**
     * Returns the email address to be used with emails send from the application.
     *
     * @return a non-null instance
     */
    String getFrom();

    /**
     * Returns the maximum retry count in case of failures.
     *
     * @return a positive integer
     */
    @DefaultValue("10")
    int getMaximumRetryCount();

    /**
     * Returns the retention of sent emails.
     *
     * @return a non-null instance
     */
    @DefaultValue("7d")
    Duration getRetention();

    /**
     * Returns the retry interval.
     *
     * @return a positive integer
     */
    @DefaultValue("60s")
    Duration getRetryInterval();
}
