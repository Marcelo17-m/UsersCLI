package jalau.cis.api.controller;

import jalau.cis.api.config.SecurityConfig;
import jalau.cis.api.config.SecurityErrorResponseWriter;
import jalau.cis.api.service.UserService;
import jalau.cis.api.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, SecurityErrorResponseWriter.class})
class HealthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private JdbcTemplate jdbcTemplate;
    @MockBean  private JwtUtil jwtUtil;
    @MockBean  private UserService userService;

    @Test
    void health_dbReachable_returns200WithUpStatus() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(1);

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.databaseConnected").value(true));
    }

    @Test
    void health_dbUnreachable_returns503WithDegradedStatus() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        mockMvc.perform(get("/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.databaseConnected").value(false));
    }
}
