package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User update request payload")
public record UserUpdateRequest(

    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Schema(description = "Full name of the user", example = "Javier Roca Updated") String name,

    @Size(min = 3, max = 20, message = "Login must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Login must contain only letters, numbers, or underscores")
    @Schema(description = "Unique login username", example = "jroca2") String login,

    @Size(max = 200, message = "Password must not exceed 200 characters")
    @Schema(description = "Base64-encoded password (optional)", example = "cGFzczEyMw==") String password) {
}
