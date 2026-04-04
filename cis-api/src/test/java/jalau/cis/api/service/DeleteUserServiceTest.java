package jalau.cis.api.service;

import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

    @Mock  private UserRepository userRepository;
    @InjectMocks private DeleteUserService deleteUserService;

    @Test
    void delete_userExists_setsActiveToFalseAndReturnsTrue() {
        User user = aUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        boolean result = deleteUserService.delete(USER_ID);

        assertThat(result).isTrue();
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_userNotFound_returnsFalse() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        boolean result = deleteUserService.delete("unknown");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).delete(any());
    }
}
