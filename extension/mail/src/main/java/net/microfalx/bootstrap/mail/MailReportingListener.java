package net.microfalx.bootstrap.mail;

import lombok.extern.slf4j.Slf4j;
import net.microfalx.bootstrap.support.report.ReportingListener;
import net.microfalx.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class MailReportingListener implements ReportingListener {

    @Autowired private MailService mailService;

    @Override
    public boolean send(Set<String> destinations, String title, Resource summary, Optional<Resource> attachment) {
        for (String destination : destinations) {
            try {
                Collection<Resource> attachments = attachment.map(Collections::singleton).orElse(Collections.emptySet());
                mailService.send(destination, title, summary, attachments);
            } catch (Exception e) {
                LOGGER.warn("Failed to send report to {}", destination, e);
            }
        }
        return true;
    }
}
