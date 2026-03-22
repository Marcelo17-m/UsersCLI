package jalau.cis.api.service;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Base64;
import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @InjectMocks private UpdateUserService updateUserService;

    @Test
    void update_allFields_updatesSuccessfully() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        UserDto dto = new UserDto("New Name", "newlogin", USER_PASS_B64);
        User result = updateUserService.update(USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getLogin()).isEqualTo("newlogin");
        assertThat(result.getPassword()).isEqualTo(USER_PASS);   // decoded from Base64
    }

    @Test
    void update_onlyName_leavesOtherFieldsUnchanged() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        UserDto dto = new UserDto("Only Name", null, null);
        User result = updateUserService.update(USER_ID, dto);

        assertThat(result.getName()).isEqualTo("Only Name");
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(result.getPassword()).isEqualTo(USER_PASS);
    }

    @Test
    void update_invalidBase64Password_storesRawValue() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        UserDto dto = new UserDto(null, null, "not-valid-base64!!!");
        User result = updateUserService.update(USER_ID, dto);

        // fallback: stores the raw value when Base64 decoding fails
        assertThat(result.getPassword()).isEqualTo("not-valid-base64!!!");
    }

    @Test
    void update_userNotFound_returnsNull() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        User result = updateUserService.update("unknown", aUserDto());

        assertThat(result).isNull();
    }
}
