package net.microfalx.bootstrap.mail;

import jakarta.mail.internet.MimeMessage;
import net.microfalx.resource.ClassPathResource;
import net.microfalx.resource.MemoryResource;
import net.microfalx.resource.TemporaryFileResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSenderImpl mailSender;

    @InjectMocks
    private MailService mailService;

    @Test
    void initialize() throws Exception {
        mailService.afterPropertiesSet();
    }

    @Test
    void send() {
        mockMessageCreation();
        mailService.send("test@company.com", "Test Subject", MemoryResource.create("Test Body"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendFromResource() throws IOException {
        MimeMessage mimeMessage = createMockMessage();
        when(mailSender.createMimeMessage(any(InputStream.class))).thenReturn(mimeMessage);
        mailService.send(ClassPathResource.file("mime-message/test1.eml"));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendWithAttachment() {
        mockMessageCreation();
        mailService.send("test@company.com", "Test Subject", MemoryResource.create("Test Body"),
                List.of(TemporaryFileResource.file("temp.txt")));
        verify(mailSender).send(any(MimeMessage.class));
    }

    private void mockMessageCreation() {
        MimeMessage mimeMessage = createMockMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    private MimeMessage createMockMessage() {
        return mock(MimeMessage.class);
    }
}