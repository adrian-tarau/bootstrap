package net.microfalx.bootstrap.feature;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("bootstrap.feature")
@Setter
@Getter
@ToString
public class FeatureProperties {

    private Map<String, Boolean> activation = new HashMap<>();

}
