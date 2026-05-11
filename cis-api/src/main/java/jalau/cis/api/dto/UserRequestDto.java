package jalau.cis.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User request payload")
public class UserRequestDto {

    @Size(max = 200)
    @Schema(description = "User full name", example = "John Doe")
    private String name;

    @NotBlank(message = "Login is required")
    @Size(max = 20)
    @Schema(description = "User login username", example = "johndoe")
    private String login;

    @Size(max = 100)
    @Schema(description = "User password encoded in Base64", example = "cGFzc3dvcmQxMjM=")
    private String password;
}
