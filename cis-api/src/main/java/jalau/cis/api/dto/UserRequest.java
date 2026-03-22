package jalau.cis.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "ID is required")
    @Size(max = 36)
    private String id;

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
