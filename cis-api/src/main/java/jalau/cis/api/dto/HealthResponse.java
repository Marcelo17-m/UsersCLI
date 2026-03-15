package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for the health-check endpoint.
 */
@Schema(description = "Health-check response payload")
public record HealthResponse(

    @Schema(description = "Overall API status", example = "UP") String status,

    @Schema(description = "Server timestamp (ISO-8601)", example = "2025-03-15T14:00:00") String timestamp,

    @Schema(description = "Whether the database connection is alive", example = "true") boolean databaseConnected,

    @Schema(description = "Human-readable database connectivity message", example = "Connected to MySQL (sd3)") String databaseMessage) {
}
