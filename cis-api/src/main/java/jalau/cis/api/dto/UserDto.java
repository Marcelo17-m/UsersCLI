package jalau.cis.api.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User update request payload")
public record UserDto (

        @Schema(description = "Full name of the user", example = "Andres Medrano")
        String name,

        @Schema(description = "Login username", example = "Amedrano")
        String login,

        @Schema(description = "User password", example = "pass123")
        String password
)
{}
