package jalau.cis.api.service;

import jalau.cis.api.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock  private JdbcTemplate jdbcTemplate;
    @InjectMocks private UserService userService;

    @Test
    void findByLogin_found_returnsUserResponse() {
        UserResponse expected = aUserResponse();
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(USER_LOGIN)))
                .thenReturn(expected);

        UserResponse result = userService.findByLogin(USER_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(result.getName()).isEqualTo(USER_NAME);
    }

    @Test
    void findByLogin_notFound_returnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("ghost")))
                .thenThrow(new EmptyResultDataAccessException(1));

        UserResponse result = userService.findByLogin("ghost");

        assertThat(result).isNull();
    }
}
