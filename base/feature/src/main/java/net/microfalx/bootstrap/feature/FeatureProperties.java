package net.microfalx.bootstrap.feature;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.feature")
@Setter
@Getter
@ToString
public class FeatureProperties {

}
