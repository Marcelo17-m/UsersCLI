package jalau.cis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jalau.cis.api.dto.UserDto;
import jalau.cis.api.model.User;
import jalau.cis.api.service.DeleteUserService;
import jalau.cis.api.service.ReadUsersService;
import jalau.cis.api.service.UpdateUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    @Autowired
    private UpdateUserService updateUserService;

    @Autowired
    private DeleteUserService deleteUserService;

    @Autowired
    private ReadUsersService readUserService;

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates specific fields of an existing user.", responses = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> updateUser(
            @PathVariable String id,
            @RequestBody UserDto dto) {

        User updated = updateUserService.update(id, dto);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disable a user", description = "Marks a user as inactive (soft delete).", responses = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })

    public ResponseEntity<?> deleteUser(@PathVariable String id) {

        boolean deleted = deleteUserService.delete(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("User disabled successfully");
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns the complete list of the users", responses = {
            @ApiResponse(responseCode = "200", description = "List of users",
                        content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = User.class)))
    })
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(readUserService.readAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Returns the details of a single user", responses = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getUser(@PathVariable String id) {
        User user = readUserService.read(id);

        if (user == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

}
