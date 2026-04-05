package jalau.cis.api.service;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.exception.DuplicateLoginException;
import jalau.cis.api.exception.UserNotFoundException;
import jalau.cis.api.mapper.UserMapper;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    void findByLogin_found_returnsUserResponse() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(aUser()));
        when(userMapper.toResponseDto(any(UserModel.class))).thenReturn(aUserResponse());

        UserResponseDto result = userService.findByLogin(USER_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(result.getName()).isEqualTo(USER_NAME);
    }

    @Test
    void findByLogin_notFound_throwsUserNotFoundException() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByLogin("ghost"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found.");
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(aUserList());
        when(userMapper.toResponseDto(any(UserModel.class))).thenReturn(aUserResponse());

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
        when(userMapper.toResponseDto(any(UserModel.class))).thenReturn(aUserResponse());

        UserResponseDto result = userService.findById(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(USER_ID);
        assertThat(result.getName()).isEqualTo(USER_NAME);
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
    }

    @Test
    void findById_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found.");
    }

    @Test
    void update_allFields_updatesSuccessfully() {
        UserModel savedUser = aUser();
        savedUser.setName("New Name");
        savedUser.setLogin("newlogin");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(UserResponseDto.builder()
                .id(USER_ID)
                .name("New Name")
                .login("newlogin")
                .active(true)
                .build());

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
        when(userMapper.toResponseDto(savedUser)).thenReturn(UserResponseDto.builder()
                .id(USER_ID)
                .name("Only Name")
                .login(USER_LOGIN)
                .active(true)
                .build());

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
        when(userMapper.toResponseDto(savedUser)).thenReturn(UserResponseDto.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .login("newlogin")
                .active(true)
                .build());

        UserRequestDto dto = UserRequestDto.builder().login("newlogin").build();
        UserResponseDto result = userService.update(USER_ID, dto);

        assertThat(result.getLogin()).isEqualTo("newlogin");
        assertThat(result.getName()).isEqualTo(USER_NAME);
    }

    @Test
    void update_invalidBase64Password_storesRawValue() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toResponseDto(any(UserModel.class))).thenReturn(aUserResponse());

        UserRequestDto dto = UserRequestDto.builder().password("not-valid-base64!!!").build();
        UserResponseDto result = userService.update(USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
    }

    @Test
    void update_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update("unknown", aUserRequest()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found.");
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
    void delete_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found.");
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void create_duplicateLogin_throwsDuplicateLoginException() {
        when(userRepository.findByLogin("updatedlogin")).thenReturn(Optional.of(aUser()));

        assertThatThrownBy(() -> userService.create(aUserRequest()))
                .isInstanceOf(DuplicateLoginException.class)
                .hasMessage("User with same Login already exists.");
    }
}
