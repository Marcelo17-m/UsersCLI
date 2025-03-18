package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response with JWT token")
public class LoginResponse {

    @Schema(description = "JWT token", example = "e2513534soughui1058hola158")
    private String token;

    @Schema(description = "Response message", example = "Successful login")
    private String message;

    public LoginResponse() {}

    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
