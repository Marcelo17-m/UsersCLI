package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{login}")
    @Operation(summary = "Get user by login", description = "Returns a user based on login")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid login (empty or blank)"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserByLogin(@PathVariable String login) {
        
        // Validación de entrada
        if (login == null || login.isBlank()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Login cannot be empty");
            error.put("status", "400");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        UserResponse user = userService.findByLogin(login);
        
        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User with login '" + login + "' not found");
            error.put("status", "404");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        
        return ResponseEntity.ok(user);
    }
}
