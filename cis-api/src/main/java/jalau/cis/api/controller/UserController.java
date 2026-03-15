package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jalau.cis.api.dto.UserRequest;
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

    @Autowired
    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping
    @Operation(summary = "Register a new user", description = "Receives a Base64 'hash' and decodes it to plain text for DB storage.")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE id = ? OR login = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, request.getId(), request.getLogin());

        if (count != null && count > 0) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User with same ID or Login already exists.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(request.getPassword());
            String plainPassword = new String(decodedBytes);

            String insertSql = "INSERT INTO users (id, name, login, password) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, request.getId(), request.getName(), request.getLogin(),
                    plainPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully (stored as plain text)");
            response.put("id", request.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid 'hash' format. Password must be Base64 encoded.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
