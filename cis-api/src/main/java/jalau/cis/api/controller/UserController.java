package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jalau.cis.api.dto.UserDto;
import jalau.cis.api.dto.UserRequest;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.service.DeleteUserService;
import jalau.cis.api.service.UpdateUserService;
import jalau.cis.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final JdbcTemplate jdbcTemplate;
    private final DeleteUserService deleteUserService;
    private final UserService userService;
    private final UpdateUserService updateUserService;

    @Autowired
    public UserController(JdbcTemplate jdbcTemplate,
                          DeleteUserService deleteUserService,
                          UserService userService,
                          UpdateUserService updateUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.deleteUserService = deleteUserService;
        this.userService = userService;
        this.updateUserService = updateUserService;
    }

    @PostMapping
    @Operation(summary = "Register a new user", description = "Receives a Base64 password and decodes it for DB storage.")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request) {
        String generatedId = UUID.randomUUID().toString();

        String checkSql = "SELECT COUNT(*) FROM users WHERE login = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, request.getLogin());

        if (count != null && count > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "User with same Login already exists."));
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(request.getPassword());
            String plainPassword = new String(decodedBytes);

            String insertSql = "INSERT INTO users (id, name, login, password, active) VALUES (?, ?, ?, ?, 1)";
            jdbcTemplate.update(insertSql, generatedId, request.getName(), request.getLogin(), plainPassword);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully", "id", generatedId));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid format. Password must be Base64."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDto dto) {
        UserResponse updated = updateUserService.update(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @Operation(summary = "Get Users")
    public ResponseEntity<?> getUser(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String id) {

        if (login != null && !login.isBlank()) {
            UserResponse user = userService.findByLogin(login);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        if (id != null && !id.isBlank()) {
            UserResponse user = userService.findById(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.ok(userService.findAll());
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