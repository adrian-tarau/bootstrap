package net.microfalx.bootstrap.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import net.microfalx.lang.IdGenerator;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.lang.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;

/**
 * Holds information about the status of a mail sending operation.
 */
@Getter
@ToString
public class Mail implements Identifiable<String>, Nameable {

    private String id;
    private String subject = StringUtils.NA_STRING;
    private String to = StringUtils.NA_STRING;
    private String from = StringUtils.NA_STRING;
    volatile Status status = Status.PENDING;
    private final LocalDateTime createdAt = LocalDateTime.now();
    @Getter(AccessLevel.NONE) final AtomicInteger retryCount = new AtomicInteger();
    volatile LocalDateTime sentAt;
    volatile LocalDateTime failedAt;
    volatile Throwable throwable;

    @Getter(AccessLevel.PACKAGE)
    private volatile MimeMessage mimeMessage;

    Mail(MimeMessage mimeMessage) {
        requireNotEmpty(mimeMessage);
        this.mimeMessage = mimeMessage;
        try {
            this.id = mimeMessage.getMessageID();
            this.to = mimeMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString();
            this.from = mimeMessage.getFrom()[0].toString();
            this.subject = mimeMessage.getSubject();
        } catch (MessagingException e) {
            // ignore, use default values
        }
        if (StringUtils.isEmpty(this.id)) {
            this.id = IdGenerator.get().nextAsString();
        }
    }

    @Override
    public String getName() {
        return subject;
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    void sent() {
        this.status = Status.SENT;
        this.sentAt = LocalDateTime.now();
        this.throwable = null;
        this.mimeMessage = null;
    }

    void failed(Throwable throwable) {
        this.status = Status.FAILED;
        this.failedAt = LocalDateTime.now();
        this.throwable = throwable;
        this.retryCount.incrementAndGet();
    }

    public enum Status {
        PENDING, SENT, FAILED
    }
}
