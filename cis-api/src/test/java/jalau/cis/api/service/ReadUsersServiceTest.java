package jalau.cis.api.service;

import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import jalau.cis.api.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadUsersServiceTest {

    @Mock  private UserRepository userRepository;
    @InjectMocks private ReadUsersService readUsersService;

    @Test
    void readAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(aUserList());

        List<UserResponse> result = readUsersService.readAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(USER_ID);
    }

    @Test
    void readAll_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponse> result = readUsersService.readAll();

        assertThat(result).isEmpty();
    }

    @Test
    void read_userExists_returnsUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));

        UserResponse result = readUsersService.read(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getName()).isEqualTo(USER_NAME);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
    }

    @Test
    void read_userNotFound_returnsNull() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        UserResponse result = readUsersService.read("unknown");

        assertThat(result).isNull();
    }
}
