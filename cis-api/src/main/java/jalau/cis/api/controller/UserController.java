package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/users")
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Register a new user", description = "Receives a Base64 password and decodes it for DB storage.")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(UserResponseDto.builder().message(e.getMessage()).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(UserResponseDto.builder().message(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UserResponseDto.builder().message("Internal error: " + e.getMessage()).build());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserRequestDto dto) {
        UserResponseDto updated = userService.update(id, dto);
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
            UserResponseDto user = userService.findByLogin(login);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        }

        if (id != null && !id.isBlank()) {
            UserResponseDto user = userService.findById(id);
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
        boolean deleted = userService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResponseDto.builder().message("User disabled successfully").build());
    }
}
