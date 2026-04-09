package jalau.cis.api.controller;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.exception.DuplicateLoginException;
import jalau.cis.api.exception.UserNotFoundException;
import jalau.cis.api.service.UserService;
import jalau.cis.api.config.SecurityConfig;
import jalau.cis.api.config.SecurityErrorResponseWriter;
import jalau.cis.api.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SecurityErrorResponseWriter.class})
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;
        @MockBean
        private JwtUtil jwtUtil;

        // ------------------------------------------------------------------ POST

        @Test
        void register_success_returns201() throws Exception {
                when(userService.create(any(UserRequestDto.class))).thenReturn(
                                UserResponseDto.builder().id(USER_ID).message("User registered successfully").build());

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("User registered successfully"))
                                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void register_duplicateUser_returns409() throws Exception {
                when(userService.create(any(UserRequestDto.class)))
                                .thenThrow(new DuplicateLoginException("User with same Login already exists."));

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson()))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("USR-409"))
                                .andExpect(jsonPath("$.message").value("User with same Login already exists."))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users"));
        }

        @Test
        void register_invalidBase64Password_returns400() throws Exception {
                when(userService.create(any(UserRequestDto.class)))
                                .thenThrow(new IllegalArgumentException("Invalid format. Password must be Base64."));

                String body = "{\"name\":\"Name\",\"login\":\"login1\",\"password\":\"!!not-base64!!\"}";
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("USR-400"))
                                .andExpect(jsonPath("$.message").value("Invalid format. Password must be Base64."))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users"));
        }

        @Test
        void register_missingRequiredFields_returns400() throws Exception {
                String body = "{\"id\":\"\",\"name\":\"\",\"login\":\"\",\"password\":\"\"}";
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        // ------------------------------------------------------------------ GET all

        @Test
        @WithMockUser
        void getUsers_returns200WithList() throws Exception {
                when(userService.findAll()).thenReturn(aUserResponseList());

                mockMvc.perform(get("/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void getUsers_withoutAuth_returns401() throws Exception {
                mockMvc.perform(get("/users"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("AUTH-401"))
                                .andExpect(jsonPath("$.message").value("Unauthorized access"))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users"));
        }

        // ------------------------------------------------------------------ GET by ID

        @Test
        @WithMockUser
        void getUserById_found_returns200() throws Exception {
                when(userService.findById(USER_ID)).thenReturn(aUserResponse());

                mockMvc.perform(get("/users").param("id", USER_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(USER_ID));
        }

        @Test
        @WithMockUser
        void getUserById_notFound_returns404() throws Exception {
                when(userService.findById("unknown")).thenThrow(new UserNotFoundException("User not found."));

                mockMvc.perform(get("/users").param("id", "unknown"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("USR-404"))
                                .andExpect(jsonPath("$.message").value("User not found."))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users"));
        }

        // ------------------------------------------------------------------ GET by login

        @Test
        @WithMockUser
        void getUserByLogin_found_returns200() throws Exception {
                when(userService.findByLogin(USER_LOGIN)).thenReturn(aUserResponse());

                mockMvc.perform(get("/users").param("login", USER_LOGIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.login").value(USER_LOGIN));
        }

        @Test
        @WithMockUser
        void getUserByLogin_notFound_returns404() throws Exception {
                when(userService.findByLogin("ghost")).thenThrow(new UserNotFoundException("User not found."));

                mockMvc.perform(get("/users").param("login", "ghost"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("USR-404"))
                                .andExpect(jsonPath("$.message").value("User not found."))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users"));
        }

        // ------------------------------------------------------------------ PUT

        @Test
        @WithMockUser
        void updateUser_found_returns200() throws Exception {
                UserResponseDto updated = UserResponseDto.builder()
                                .id(USER_ID)
                                .name("Updated Name")
                                .login(USER_LOGIN)
                                .active(true)
                                .build();
                when(userService.update(eq(USER_ID), any(UserRequestDto.class))).thenReturn(updated);

                mockMvc.perform(put("/users/{id}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Name"));
        }

        @Test
        void updateUser_withoutAuth_returns401() throws Exception {
                mockMvc.perform(put("/users/{id}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson()))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("AUTH-401"))
                                .andExpect(jsonPath("$.message").value("Unauthorized access"))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users/" + USER_ID));
        }

        @Test
        @WithMockUser
        void updateUser_notFound_returns404() throws Exception {
                when(userService.update(eq("unknown"), any(UserRequestDto.class)))
                                .thenThrow(new UserNotFoundException("User not found."));

                mockMvc.perform(put("/users/{id}", "unknown")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("USR-404"))
                                .andExpect(jsonPath("$.message").value("User not found."))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users/unknown"));
        }

        // ------------------------------------------------------------------ DELETE

        @Test
        @WithMockUser
        void deleteUser_found_returns200() throws Exception {
                when(userService.delete(USER_ID)).thenReturn(true);

                mockMvc.perform(delete("/users/{id}", USER_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User disabled successfully"));
        }

        @Test
        void deleteUser_withoutAuth_returns401() throws Exception {
                mockMvc.perform(delete("/users/{id}", USER_ID))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("AUTH-401"))
                                .andExpect(jsonPath("$.message").value("Unauthorized access"))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users/" + USER_ID));
        }

        @Test
        @WithMockUser
        void deleteUser_notFound_returns404() throws Exception {
                when(userService.delete("unknown")).thenThrow(new UserNotFoundException("User not found."));

                mockMvc.perform(delete("/users/{id}", "unknown"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("USR-404"))
                                .andExpect(jsonPath("$.message").value("User not found."))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.path").value("/users/unknown"));
        }
}
