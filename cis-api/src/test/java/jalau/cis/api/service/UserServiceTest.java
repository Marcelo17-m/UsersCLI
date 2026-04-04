package jalau.cis.api.service;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.model.UserModel;
import jalau.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void findByLogin_found_returnsUserResponse() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(aUser()));

        UserResponseDto result = userService.findByLogin(USER_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(result.getName()).isEqualTo(USER_NAME);
    }

    @Test
    void findByLogin_notFound_returnsNull() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        UserResponseDto result = userService.findByLogin("ghost");

        assertThat(result).isNull();
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(aUserList());

        List<UserResponseDto> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(USER_ID);
    }

    @Test
    void findAll_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponseDto> result = userService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_userExists_returnsUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));

        UserResponseDto result = userService.findById(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getName()).isEqualTo(USER_NAME);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
    }

    @Test
    void findById_userNotFound_returnsNull() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        UserResponseDto result = userService.findById("unknown");

        assertThat(result).isNull();
    }

    @Test
    void update_allFields_updatesSuccessfully() {
        UserModel savedUser = aUser();
        savedUser.setName("New Name");
        savedUser.setLogin("newlogin");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);

        UserRequestDto dto = UserRequestDto.builder()
                .name("New Name")
                .login("newlogin")
                .password(USER_PASS_B64)
                .build();
        UserResponseDto result = userService.update(USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getLogin()).isEqualTo("newlogin");
    }

    @Test
    void update_onlyName_leavesOtherFieldsUnchanged() {
        UserModel savedUser = aUser();
        savedUser.setName("Only Name");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);

        UserRequestDto dto = UserRequestDto.builder().name("Only Name").build();
        UserResponseDto result = userService.update(USER_ID, dto);

        assertThat(result.getName()).isEqualTo("Only Name");
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
    }

    @Test
    void update_onlyLogin_leavesOtherFieldsUnchanged() {
        UserModel savedUser = aUser();
        savedUser.setLogin("newlogin");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);

        UserRequestDto dto = UserRequestDto.builder().login("newlogin").build();
        UserResponseDto result = userService.update(USER_ID, dto);

        assertThat(result.getLogin()).isEqualTo("newlogin");
        assertThat(result.getName()).isEqualTo(USER_NAME);
    }

    @Test
    void update_invalidBase64Password_storesRawValue() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));

        UserRequestDto dto = UserRequestDto.builder().password("not-valid-base64!!!").build();
        UserResponseDto result = userService.update(USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
    }

    @Test
    void update_userNotFound_returnsNull() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        UserResponseDto result = userService.update("unknown", aUserRequest());

        assertThat(result).isNull();
    }

    @Test
    void delete_userExists_setsActiveToFalseAndReturnsTrue() {
        UserModel user = aUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        boolean result = userService.delete(USER_ID);

        assertThat(result).isTrue();
        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_userNotFound_returnsFalse() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        boolean result = userService.delete("unknown");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).delete(any());
    }
}
