package jalau.cis.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    public void shouldRegisterUserSuccessfully() throws Exception {
        String plainPassword = "password123";
        String base64Password = Base64.getEncoder().encodeToString(plainPassword.getBytes());
        String userJson = "{\"id\": \"test-id\", \"name\": \"Test User\", \"login\": \"testlogin\", \"password\": \"" + base64Password + "\"}";

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(0);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        // Try without prefix if MockMvc is not picking up the servlet path
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    public void shouldReturnConflictWhenUserExists() throws Exception {
        String userJson = "{\"id\": \"existing-id\", \"name\": \"User\", \"login\": \"login\", \"password\": \"YmFzZTY0\"}";

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(1);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with same ID or Login already exists."));
    }

    @Test
    public void shouldReturnBadRequestForInvalidBase64() throws Exception {
        String userJson = "{\"id\": \"bad-b64\", \"name\": \"User\", \"login\": \"badlogin\", \"password\": \"!!!\"}";
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(0);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid format. Password must be Base64."));
    }
}
