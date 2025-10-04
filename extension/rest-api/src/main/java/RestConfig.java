import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestConfig {

    @Autowired
    private RestProperties properties;

    @Bean
    public GroupedOpenApi publicApi() {
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder().group("public")
                .packagesToScan("*");
        return builder.build();
    }

}
