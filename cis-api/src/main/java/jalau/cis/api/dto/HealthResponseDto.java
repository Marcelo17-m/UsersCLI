package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Health-check response payload")
public record HealthResponseDto(
        @Schema(description = "Overall API status", example = "UP") String status,
        @Schema(description = "Server timestamp (ISO-8601)", example = "2025-03-15T14:00:00") String timestamp,
        @Schema(description = "Whether the database connection is alive", example = "true") boolean databaseConnected,
        @Schema(description = "Human-readable database connectivity message", example = "Connected to MySQL (sd3)") String databaseMessage
) {
}
