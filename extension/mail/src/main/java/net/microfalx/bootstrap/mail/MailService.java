package net.microfalx.bootstrap.mail;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.support.report.Issue;
import net.microfalx.lang.StringUtils;
import net.microfalx.metrics.Metrics;
import net.microfalx.metrics.Timer;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.microfalx.bootstrap.mail.MailProperties.DEFAULT_FROM;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.SecretUtils.maskSecret;
import static net.microfalx.lang.StringUtils.*;
import static net.microfalx.resource.MimeType.TEXT_HTML;

@Service
@Slf4j
public class MailService implements InitializingBean {

    @Autowired(required = false)
    private MailProperties properties = new MailProperties();

    private JavaMailSenderImpl mailSender;

    @Override
    public void afterPropertiesSet() throws Exception {
        initMailSender();
    }

    /**
     * Sends a MIME message contained in the resource.
     *
     * @param resource the resource containing the MIME message
     * @throws IOException   if an I/O error occurs while reading the resource
     * @throws MailException if an error occurs while sending the MIME message
     */
    public void send(Resource resource) throws IOException, MailException {
        requireNonNull(resource);
        MimeMessage mimeMessage = mailSender.createMimeMessage(resource.getInputStream());
        doSend(mimeMessage);
    }

    /**
     * Sends a MIME message.
     *
     * @param mimeMessage the resource containing the MIME message
     * @throws IOException   if an I/O error occurs while reading the resource
     * @throws MailException if an error occurs while sending the MIME message
     */
    public void send(MimeMessage mimeMessage) throws IOException, MailException {
        doSend(mimeMessage);
    }

    /**
     * Returns the JavaMail session used by this gateway.
     *
     * @return a non-null instance
     */
    public Session getSession() {
        return mailSender.getSession();
    }

    /**
     * Sends an email.
     *
     * @param to      a non-empty email address
     * @param subject a non-null subject
     * @param body    a non-null body
     */
    public void send(String to, String subject, Resource body) {
        send(to, subject, body, Collections.emptyList());
    }

    /**
     * Sends an email with attachments.
     *
     * @param to          a non-empty email address
     * @param subject     a non-null subject
     * @param body        a non-null body
     * @param attachments a collection of attachments
     */
    public void send(String to, String subject, Resource body, Collection<Resource> attachments) {
        requireNotEmpty(to);
        requireNonNull(subject);
        requireNonNull(body);
        requireNonNull(attachments);
        MimeMessage mimeMessage = null;
        try {
            mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8.name());
            helper.setFrom(defaultIfEmpty(properties.getFrom(), DEFAULT_FROM));
            boolean html = TEXT_HTML.equals(body.getMimeType());
            helper.setText(body.loadAsString(), html);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setSentDate(new Date());
            for (Resource resource : attachments) {
                helper.addAttachment(resource.getFileName(), new UrlResource(resource.toURL()));
            }
        } catch (Exception e) {
            registerFailure(mimeMessage, e);
        }
        if (mimeMessage != null) doSend(mimeMessage);
    }

    private void doSend(MimeMessage mimeMessage) {
        requireNonNull(mimeMessage);
        String address = getFirstAddress(mimeMessage);
        try (Timer ignored = MAIL_SENDING.startTimer(address)) {
            mailSender.send(mimeMessage);
            Arrays.stream(mimeMessage.getAllRecipients()).forEach(a -> MAIL_SENT.count(getAddress(a)));
        } catch (Exception e) {
            registerFailure(mimeMessage, e);
        }
    }

    private void registerFailure(MimeMessage mimeMessage, Exception e) {
        String address = StringUtils.NA_STRING;
        if (mimeMessage != null) {
            address = getFirstAddress(mimeMessage);
        }
        MAIL_FAILED.count(address);
        Issue.create(Issue.Type.STABILITY, "Mail")
                .withDescription(e, "Failed to send email to ''{0}''", address)
                .withSeverity(Issue.Severity.HIGH).register();
    }

    private void initMailSender() {
        mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        Properties props = mailSender.getJavaMailProperties();
        if (isNotEmpty(properties.getUserName())) {
            props.put("mail.smtp.auth", "true");
            mailSender.setUsername(properties.getUserName());
            mailSender.setPassword(properties.getPassword());
        } else {
            props.put("mail.smtp.auth", "false");
        }
        props.put("mail.smtp.ssl.enable", properties.isTls());
        props.put("mail.smtp.starttls.enable", !properties.isTls());
        LOGGER.info("SMTP settings: {}, port: {}, user: {}", properties.getHost(), properties.getPort(),
                maskSecret(properties.getUserName()));
    }

    private static String getFirstAddress(MimeMessage mimeMessage) {
        try {
            Address[] allRecipients = mimeMessage.getAllRecipients();
            if (allRecipients != null && allRecipients.length > 0) {
                return getAddress(allRecipients[0]);
            } else {
                return NA_STRING;
            }
        } catch (MessagingException e) {
            return NA_STRING;
        }
    }

    private static String getAddress(Address address) {
        if (address instanceof InternetAddress internetAddress) {
            return internetAddress.getAddress();
        } else if (address != null) {
            return address.toString();
        } else {
            return NA_STRING;
        }
    }

    private static final Metrics MAIL = Metrics.of("Mail");
    private static final Metrics MAIL_SENDING = MAIL.withGroup("Sending");
    private static final Metrics MAIL_SENT = MAIL.withGroup("Sent");
    private static final Metrics MAIL_FAILED = MAIL.withGroup("Failed");

}
