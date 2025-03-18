package jalau.cis.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CIS REST API.
 *
 * <p>
 * All endpoints live under the base path configured in
 * {@code application.properties}
 * ({@code /api/v1/}). Swagger UI is accessible at
 * <a href=
 * "http://localhost:8080/swagger-ui.html">http://localhost:8080/swagger-ui.html</a>.
 */
@SpringBootApplication
public class CisApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(CisApiApplication.class, args);
  }
}
