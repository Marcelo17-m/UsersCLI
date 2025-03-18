package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "User registration request payload")
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Login is required")
    @Size(max = 20)
    private String login;

    @NotBlank(message = "Password is required")
    @Size(max = 100)
    private String password;
}
