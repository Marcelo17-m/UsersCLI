package jalau.cis.api.mapper;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.model.UserModel;
import org.junit.jupiter.api.Test;

import static jalau.cis.api.util.TestDataFactory.USER_ID;
import static jalau.cis.api.util.TestDataFactory.USER_LOGIN;
import static jalau.cis.api.util.TestDataFactory.USER_NAME;
import static jalau.cis.api.util.TestDataFactory.USER_PASS;
import static jalau.cis.api.util.TestDataFactory.aUser;
import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toResponseDto_nullUser_returnsNull() {
        assertThat(userMapper.toResponseDto(null)).isNull();
    }

    @Test
    void toResponseDto_mapsFields() {
        UserResponseDto response = userMapper.toResponseDto(aUser());

        assertThat(response.getId()).isEqualTo(USER_ID);
        assertThat(response.getName()).isEqualTo(USER_NAME);
        assertThat(response.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void toNewModel_createsActiveUser() {
        UserRequestDto request = UserRequestDto.builder()
                .name(USER_NAME)
                .login(USER_LOGIN)
                .build();

        UserModel user = userMapper.toNewModel(request, USER_ID, USER_PASS);

        assertThat(user.getId()).isEqualTo(USER_ID);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(user.getPassword()).isEqualTo(USER_PASS);
        assertThat(user.getActive()).isTrue();
    }

    @Test
    void updateModel_ignoresNullsAndBlanks() {
        UserModel user = aUser();
        UserRequestDto request = UserRequestDto.builder()
                .name(" ")
                .login(null)
                .build();

        userMapper.updateModel(user, request, null);

        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.getLogin()).isEqualTo(USER_LOGIN);
        assertThat(user.getPassword()).isEqualTo(USER_PASS);
    }

    @Test
    void updateModel_appliesProvidedValues() {
        UserModel user = aUser();
        UserRequestDto request = UserRequestDto.builder()
                .name("Updated Name")
                .login("updatedlogin")
                .build();

        userMapper.updateModel(user, request, "decoded-password");

        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getLogin()).isEqualTo("updatedlogin");
        assertThat(user.getPassword()).isEqualTo("decoded-password");
    }
}
