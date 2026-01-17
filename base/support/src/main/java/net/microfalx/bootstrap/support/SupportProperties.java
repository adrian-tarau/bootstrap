package net.microfalx.bootstrap.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.support")
@Setter
@Getter
@ToString
public class SupportProperties {

}
