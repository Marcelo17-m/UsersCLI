package jalau.cis.api.controller;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.service.DeleteUserService;
import jalau.cis.api.service.UpdateUserService;
import jalau.cis.api.service.UserService;
import jalau.cis.api.config.SecurityConfig;
import jalau.cis.api.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private JdbcTemplate jdbcTemplate;
        @MockBean
        private DeleteUserService deleteUserService;
        @MockBean
        private UserService userService;
        @MockBean
        private UpdateUserService updateUserService;
        @MockBean
        private JwtUtil jwtUtil;

        // ------------------------------------------------------------------ POST

        @Test
        void register_success_returns201() throws Exception {
                when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(0);
                when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("User registered successfully"))
                                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void register_duplicateUser_returns409() throws Exception {
                when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(1);

                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerJson()))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("User with same Login already exists."));
        }

        @Test
        void register_invalidBase64Password_returns400() throws Exception {
                when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any())).thenReturn(0);

                String body = "{\"name\":\"Name\",\"login\":\"login1\",\"password\":\"!!not-base64!!\"}";
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Invalid format. Password must be Base64."));
        }

        @Test
        void register_missingRequiredFields_returns400WithFieldErrors() throws Exception {
                String body = "{\"name\":\"\",\"login\":\"\",\"password\":\"\"}";
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation failed"))
                                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        void register_invalidLoginFormat_returns400WithFieldErrors() throws Exception {
                String body = "{\"name\":\"Valid Name\",\"login\":\"inv@lid!\",\"password\":\"cGFzczEyMw==\"}";
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation failed"))
                                .andExpect(jsonPath("$.errors[0].field").value("login"));
        }

        @Test
        void register_nameTooShort_returns400WithFieldErrors() throws Exception {
                String body = "{\"name\":\"X\",\"login\":\"validlogin\",\"password\":\"cGFzczEyMw==\"}";
                mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation failed"))
                                .andExpect(jsonPath("$.errors[0].field").value("name"));
        }

        @Test
        void getUsers_returns200WithList() throws Exception {
                when(userService.findAll()).thenReturn(aUserResponseList());

                mockMvc.perform(get("/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)));
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
                when(userService.findById("unknown")).thenReturn(null);

                mockMvc.perform(get("/users").param("id", "unknown"))
                                .andExpect(status().isNotFound());
        }

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
                when(userService.findByLogin("ghost")).thenReturn(null);

                mockMvc.perform(get("/users").param("login", "ghost"))
                                .andExpect(status().isNotFound());
        }

        // ------------------------------------------------------------------ PUT

        @Test
        @WithMockUser
        void updateUser_found_returns200() throws Exception {
                UserResponse updated = new UserResponse(USER_ID, "Updated Name", USER_LOGIN);
                when(updateUserService.update(eq(USER_ID), any(UserDto.class))).thenReturn(updated);

                mockMvc.perform(put("/users/{id}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Name"));
        }

        @Test
        @WithMockUser
        void updateUser_notFound_returns404() throws Exception {
                when(updateUserService.update(eq("unknown"), any(UserUpdateRequest.class))).thenReturn(null);

                mockMvc.perform(put("/users/{id}", "unknown")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson()))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void updateUser_invalidLoginFormat_returns400() throws Exception {
                String body = "{\"login\":\"inv@lid!!\"}";
                mockMvc.perform(put("/users/{id}", USER_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Validation failed"))
                                .andExpect(jsonPath("$.errors[0].field").value("login"));
        }

        @Test
        @WithMockUser
        void deleteUser_found_returns200() throws Exception {
                when(deleteUserService.delete(USER_ID)).thenReturn(true);

                mockMvc.perform(delete("/users/{id}", USER_ID))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("User disabled successfully"));
        }

        @Test
        @WithMockUser
        void deleteUser_notFound_returns404() throws Exception {
                when(deleteUserService.delete("unknown")).thenReturn(false);

                mockMvc.perform(delete("/users/{id}", "unknown"))
                                .andExpect(status().isNotFound());
        }
}
