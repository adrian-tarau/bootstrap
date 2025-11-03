package net.microfalx.bootstrap.mail;

import jakarta.mail.internet.MimeMessage;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Service
public class MailService implements InitializingBean {

    @Autowired(required = false)
    private MailProperties properties = new MailProperties();

    private JavaMailSenderImpl mailSender;

    @Override
    public void afterPropertiesSet() throws Exception {
        initMailSender();
    }

    /**
     * Sends an email.
     *
     * @param to      a non-empty email address
     * @param subject a non-null subject
     * @param body    a non-null body
     */
    public void send(String to, String subject, Resource body) {
        send(to, subject, body, null);
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
        try {
            MimeMessage mimeMessage = createBody(body);
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setSentDate(new Date());
            if (attachments != null) {
                for (Resource a : attachments) {
                    InputStreamSource source = new UrlResource(a.toURL());
                    helper.addAttachment(a.getFileName(), source);
                }
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new MailSendException("The mail could not be send", e);
        }

    }

    private MimeMessage createBody(Resource body) throws IOException {
        return mailSender.createMimeMessage(body.getInputStream());
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
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", properties.isTls());
    }

}
