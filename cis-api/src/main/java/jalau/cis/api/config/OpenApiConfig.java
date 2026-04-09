package jalau.cis.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String port;

    @Value("${spring.mvc.servlet.path:}")
    private String basePath;

    @Bean
    public OpenAPI cisOpenAPI() {
        String url = "http://localhost:" + port + basePath;

        return new OpenAPI()
            .info(new Info()
                .title("CIS API")
                .description("Cohort Information System – REST API (OpenAPI 3)")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Jala University – SD3 Team")
                    .email("sd3@jalauniversity.com"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url(url)
                    .description("Dynamic Local Server")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
            .components(new Components()
                    .addSecuritySchemes("Bearer Token", new SecurityScheme()
                            .name("Bearer Token")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")));
    }
}
