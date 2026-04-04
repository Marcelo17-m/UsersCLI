package jalau.cis.api.service;

import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock  private UserRepository userRepository;
    @InjectMocks private UserService userService;

    @Test
    void findByLogin_found_returnsUserResponse() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(aUser()));

        UserResponse result = userService.findByLogin(USER_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(result.getName()).isEqualTo(USER_NAME);
    }

    @Test
    void findByLogin_notFound_returnsNull() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        UserResponse result = userService.findByLogin("ghost");

        assertThat(result).isNull();
    }
}
