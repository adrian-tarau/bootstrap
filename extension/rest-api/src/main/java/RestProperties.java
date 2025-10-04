import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("bootstrap.rest.api")
@Getter
@Setter
public class RestProperties {

    private String uiPath = "/api";
    private boolean uiEnabled = true;
    private String specsPath = "/api/spec";
    private boolean specEnabled = true;
}
