package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jalau.cis.api.dto.ErrorResponseDto;
import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // </editor-fold>

    // <editor-fold desc="Update Logic">

    /**
     * Updates the data of an existing user by ID.
     * @param id The unique identifier of the user to update.
     * @param dto New user data to apply.
     * @return Updated user wrapped in UserResponseDto.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String id, @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    // <editor-fold desc="Read Logic">

    /**
     * Retrieves users by optial filters (login or id), or returns all users if no filter is provided.
     * @param login Optional login filter.
     * @param id Optional user ID filter.
     * @return Matching user(s) wrapped in ResponseEntity
     */
    @GetMapping
    @Operation(summary = "Get Users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<?> getUser(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String id) {

        if (login != null && !login.isBlank()) {
            return ResponseEntity.ok(userService.findByLogin(login));
        }

        if (id != null && !id.isBlank()) {
            return ResponseEntity.ok(userService.findById(id));
        }

        return ResponseEntity.ok(userService.findAll());
    }

    // </editor-fold>

    // <editor-fold desc="Deletion Logic">

    /**
     * Disables a user account using JWT context for audit traceability.
     * @param id The unique identifier of the user to disable.
     * @param authentication Injected JWT authentication context for audit logging.
     * @return Success message wrapped in UserResponseDto.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Disable a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<?> deleteUser(@PathVariable String id, Authentication authentication) {
        String loginName = authentication != null ? authentication.getName() : null;
        userService.delete(id, loginName);
        return ResponseEntity.ok(UserResponseDto.builder().message("User disabled successfully").build());
    }

    // </editor-fold>
}
