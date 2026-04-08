package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication request")
public class AuthRequestDto {

    @NotBlank(message = "Login is required")
    @Schema(description = "Username", example = "tita123")
    private String login;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password encoded in Base64", example = "cGFzc3dvcmQxMjM=")
    private String password;

    public AuthRequestDto() {
    }

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
