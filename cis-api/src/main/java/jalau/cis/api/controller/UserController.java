package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jalau.cis.api.dto.UserDto;
import jalau.cis.api.dto.UserRequest;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.service.DeleteUserService;
import jalau.cis.api.service.ReadUsersService;
import jalau.cis.api.service.UpdateUserService;
import jalau.cis.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final JdbcTemplate jdbcTemplate;
    private final ReadUsersService readUserService;
    private final DeleteUserService deleteUserService;
    private final UserService userService;
    private final UpdateUserService updateUserService;

    @Autowired
    public UserController(JdbcTemplate jdbcTemplate,
                          ReadUsersService readUserService,
                          DeleteUserService deleteUserService,
                          UserService userService,
                          UpdateUserService updateUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.readUserService = readUserService;
        this.deleteUserService = deleteUserService;
        this.userService = userService;
        this.updateUserService = updateUserService;
    }

    @PostMapping
    @Operation(summary = "Register a new user", description = "Receives a Base64 password and decodes it for DB storage.")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ? OR login = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, request.getId(), request.getLogin());

        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "User with same ID or Login already exists."));
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(request.getPassword());
            String plainPassword = new String(decodedBytes);

            String insertSql = "INSERT INTO users (id, name, login, password, active) VALUES (?, ?, ?, ?, 1)";
            jdbcTemplate.update(insertSql, request.getId(), request.getName(), request.getLogin(), plainPassword);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully", "id", request.getId()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid format. Password must be Base64."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error: " + e.getMessage()));
        }
    }

    @GetMapping("/login/{login}")
    @Operation(summary = "Get user by login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getUserByLogin(@PathVariable String login) {
        if (login == null || login.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Login cannot be empty"));
        }

        UserResponse user = userService.findByLogin(login);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found", "status", "404"));
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDto dto) {
        User updated = updateUserService.update(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(readUserService.readAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        User user = readUserService.read(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disable a user")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        boolean deleted = deleteUserService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "User disabled successfully"));
    }
}