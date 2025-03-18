package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank(message = "Login is required")
    @Schema(description = "Username", example = "tita123")
    private String login;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password in plain text", example = "muzques123")
    private String password;

    public LoginRequest() {}

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
