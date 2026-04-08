package jalau.cis.api.service;

import jalau.cis.api.dto.AuthRequestDto;
import jalau.cis.api.dto.AuthResponseDto;
import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.exception.DuplicateLoginException;
import jalau.cis.api.exception.InactiveUserException;
import jalau.cis.api.exception.InvalidCredentialsException;
import jalau.cis.api.model.UserModel;
import jalau.cis.api.repository.UserRepository;
import jalau.cis.api.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    // ------------------------------------------------------------------ login

    @Test
    void login_validCredentials_returnsTokenResponse() {
        UserModel user = aUser();
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(USER_LOGIN)).thenReturn("jwt-token");

        AuthRequestDto request = new AuthRequestDto();
        request.setLogin(USER_LOGIN);
        request.setPassword(USER_PASS_B64);

        AuthResponseDto response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getMessage()).isEqualTo("Successful login");
    }

    @Test
    void login_userNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        AuthRequestDto request = new AuthRequestDto();
        request.setLogin("ghost");
        request.setPassword("any");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid Credentials");
    }

    @Test
    void login_inactiveUser_throwsInactiveUserException() {
        UserModel user = aUser();
        user.setActive(false);
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(user));

        AuthRequestDto request = new AuthRequestDto();
        request.setLogin(USER_LOGIN);
        request.setPassword(USER_PASS_B64);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InactiveUserException.class)
                .hasMessage("Inactive User");
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(aUser()));

        AuthRequestDto request = new AuthRequestDto();
        request.setLogin(USER_LOGIN);
        request.setPassword("wrongpassword");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid Credentials");
    }

    // ------------------------------------------------------------------ register

    @Test
    void register_validRequest_savesUserWithDecodedPassword() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> i.getArgument(0));

        UserRequestDto request = UserRequestDto.builder()
                .name(USER_NAME)
                .login(USER_LOGIN)
                .password(USER_PASS_B64)
                .build();

        authService.register(request);

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(captor.capture());
        UserModel saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo(USER_NAME);
        assertThat(saved.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(saved.getPassword()).isEqualTo(USER_PASS);
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    void register_duplicateLogin_throwsDuplicateLoginException() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.of(aUser()));

        UserRequestDto request = UserRequestDto.builder()
                .name(USER_NAME)
                .login(USER_LOGIN)
                .password(USER_PASS_B64)
                .build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateLoginException.class)
                .hasMessage("User with same Login already exists.");
    }

    @Test
    void register_invalidBase64Password_throwsIllegalArgumentException() {
        when(userRepository.findByLogin(USER_LOGIN)).thenReturn(Optional.empty());

        UserRequestDto request = UserRequestDto.builder()
                .name(USER_NAME)
                .login(USER_LOGIN)
                .password("!!not-base64!!")
                .build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid format. Password must be Base64.");
    }
}
