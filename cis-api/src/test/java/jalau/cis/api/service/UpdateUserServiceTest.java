package jalau.cis.api.service;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static jalau.cis.api.util.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

        @Mock
        private UserRepository userRepository;
        @InjectMocks
        private UpdateUserService updateUserService;

        @Test
        void update_allFields_updatesSuccessfully() {
                User savedUser = aUser();
                savedUser.setName("New Name");
                savedUser.setLogin("newlogin");

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                UserDto dto = new UserDto("New Name", "newlogin", USER_PASS_B64);
                UserResponse result = updateUserService.update(USER_ID, dto);

                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("New Name");
                assertThat(result.getLogin()).isEqualTo("newlogin");
        }

        @Test
        void update_onlyName_leavesOtherFieldsUnchanged() {
                User savedUser = aUser();
                savedUser.setName("Only Name");

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                UserDto dto = new UserDto("Only Name", null, null);
                UserResponse result = updateUserService.update(USER_ID, dto);

                assertThat(result.getName()).isEqualTo("Only Name");
                assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        }

        @Test
        void update_onlyLogin_leavesOtherFieldsUnchanged() {
                User savedUser = aUser();
                savedUser.setLogin("newlogin");

                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
                when(userRepository.save(any(User.class))).thenReturn(savedUser);

                UserDto dto = new UserDto(null, "newlogin", null);
                UserResponse result = updateUserService.update(USER_ID, dto);

                assertThat(result.getLogin()).isEqualTo("newlogin");
                assertThat(result.getName()).isEqualTo(USER_NAME);
        }

        @Test
        void update_invalidBase64Password_storesRawValue() {
                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(aUser()));
                when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

                UserDto dto = new UserDto(null, null, "not-valid-base64!!!");
                UserResponse result = updateUserService.update(USER_ID, dto);

                assertThat(result).isNotNull();
                assertThat(result.getLogin()).isEqualTo(USER_LOGIN);
        }

        @Test
        void update_userNotFound_returnsNull() {
                when(userRepository.findById("unknown")).thenReturn(Optional.empty());

                UserResponse result = updateUserService.update("unknown", aUserDto());

                assertThat(result).isNull();
        }
}
