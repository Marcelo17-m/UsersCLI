package jalau.cis.api.controller;

import jalau.cis.api.config.SecurityConfig;
import jalau.cis.api.config.SecurityErrorResponseWriter;
import jalau.cis.api.dto.AuthResponseDto;
import jalau.cis.api.service.AuthService;
import jalau.cis.api.service.UserService;
import jalau.cis.api.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SecurityErrorResponseWriter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserService userService;

    private static final String LOGIN_JSON = "{\"login\":\"testlogin\",\"password\":\"cGFzc3dvcmQxMjM=\"}";
    private static final String REGISTER_JSON =
            "{\"name\":\"Test User\",\"login\":\"testlogin\",\"password\":\"cGFzc3dvcmQxMjM=\"}";

    // ------------------------------------------------------------------ POST /auth/login

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponseDto("jwt-token", "Successful login"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.message").value("Successful login"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("Invalid Credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Credentials"));
    }

    @Test
    void login_inactiveUser_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new RuntimeException("Inactive User"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Inactive User"));
    }

    @Test
    void login_missingLogin_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ POST /auth/register

    @Test
    void register_success_returns204() throws Exception {
        doNothing().when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void register_duplicateLogin_returns409() throws Exception {
        doThrow(new IllegalStateException("User with same Login already exists."))
                .when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with same Login already exists."));
    }

    @Test
    void register_invalidBase64Password_returns400() throws Exception {
        doThrow(new IllegalArgumentException("Invalid format. Password must be Base64."))
                .when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"login\":\"login1\",\"password\":\"!!not-base64!!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid format. Password must be Base64."));
    }

    @Test
    void register_missingLogin_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"password\":\"cGFzc3dvcmQxMjM=\"}"))
                .andExpect(status().isBadRequest());
    }
}
