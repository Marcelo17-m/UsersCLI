package jalau.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response object (without password)")
public class UserResponse {
    
    @Schema(description = "User unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;
    
    @Schema(description = "User full name", example = "John Doe")
    private String name;
    
    @Schema(description = "User login username", example = "johndoe")
    private String login;

    public UserResponse() {}

    public UserResponse(String id, String name, String login) {
        this.id = id;
        this.name = name;
        this.login = login;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
}
