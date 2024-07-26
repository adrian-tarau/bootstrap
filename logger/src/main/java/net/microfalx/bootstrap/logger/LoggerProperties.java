package net.microfalx.bootstrap.logger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@ConfigurationProperties("bootstrap.logger")
@Getter
@Setter
@ToString
public class LoggerProperties {

    private boolean debug;
    private boolean trace;
    private String application;
    private String process;
    private Syslog syslog = new Syslog();
    private Gelf gelf = new Gelf();

    public enum Protocol {
        TCP,
        UDP
    }

    @Getter
    @Setter
    @ToString
    public static class Remote {
        private String hostname;
        private int port = -1;
        private Protocol protocol = Protocol.UDP;
        private String facility = "user";
        private boolean onlyAlerts = true;

        public URI toUri() {
            return URI.create(protocol.name().toLowerCase() + "://" + hostname + ":" + port);
        }
    }


    public static class Syslog extends Remote {

        public Syslog() {
            setPort(2514);
        }
    }


    public static class Gelf extends Remote {

        public Gelf() {
            setPort(12201);
        }
    }
}
