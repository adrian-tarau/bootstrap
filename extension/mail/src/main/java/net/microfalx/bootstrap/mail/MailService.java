package net.microfalx.bootstrap.mail;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.microfalx.resource.MimeType;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.SecretUtils.maskSecret;
import static net.microfalx.lang.StringUtils.isNotEmpty;

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
        mailSender.send(mimeMessage);
    }

    /**
     * Sends a MIME message.
     *
     * @param mimeMessage the resource containing the MIME message
     * @throws IOException   if an I/O error occurs while reading the resource
     * @throws MailException if an error occurs while sending the MIME message
     */
    public void send(MimeMessage mimeMessage) throws IOException, MailException {
        requireNonNull(mimeMessage);
        mailSender.send(mimeMessage);
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
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getFrom());
            boolean html = MimeType.TEXT_HTML.equals(body.getMimeType());
            helper.setText(body.loadAsString(), html);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setSentDate(new Date());
            for (Resource resource : attachments) {
                helper.addAttachment(resource.getFileName(), new UrlResource(resource.toURL()));
            }
            send(mimeMessage);
        } catch (Exception e) {
            throw new MailSendException("The mail could not be send", e);
        }

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

}
