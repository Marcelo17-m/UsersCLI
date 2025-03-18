package jalau.cis.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger configuration.
 *
 * <p>
 * Swagger UI → <a href="http://localhost:8080/swagger-ui.html">
 * http://localhost:8080/swagger-ui.html</a>
 * <p>
 * JSON spec → <a href="http://localhost:8080/api-docs">
 * http://localhost:8080/api-docs</a>
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI cisOpenAPI() {
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
                .url("http://localhost:8080/api/v1")
                .description("Local Development Server")));
  }
}
