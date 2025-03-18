package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jalau.cis.api.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Health-check controller.
 *
 * <p>
 * Verifies that the API is running and that the MySQL database is reachable.
 * Accessible at {@code GET /api/v1/health}.
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "API health-check endpoints")
public class HealthController {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public HealthController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Returns the current status of the API and its database connection.
   *
   * @return {@link HealthResponse} with status, timestamp, and DB connectivity
   *         flag.
   */
  @GetMapping
  @Operation(summary = "API Health Check", description = "Returns the API status and verifies connectivity to the MySQL database.", responses = {
      @ApiResponse(responseCode = "200", description = "API is up and running", content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthResponse.class))),
      @ApiResponse(responseCode = "503", description = "API is up but the database is unreachable")
  })
  public ResponseEntity<HealthResponse> health() {
    boolean dbOk = false;
    String dbMessage;

    try {
      jdbcTemplate.queryForObject("SELECT 1", Integer.class);
      dbOk = true;
      dbMessage = "Connected to MySQL (sd3)";
    } catch (Exception ex) {
      dbMessage = "Database unreachable: " + ex.getMessage();
    }

    HealthResponse response = new HealthResponse(
        dbOk ? "UP" : "DEGRADED",
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        dbOk,
        dbMessage);

    return dbOk
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(503).body(response);
  }
}
