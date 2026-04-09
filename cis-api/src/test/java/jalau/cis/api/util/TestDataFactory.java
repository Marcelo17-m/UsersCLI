package jalau.cis.api.util;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.model.UserModel;

import java.util.Base64;
import java.util.List;

public final class TestDataFactory {

    public static final String USER_ID = "test-id-001";
    public static final String USER_NAME = "Test User";
    public static final String USER_LOGIN = "testlogin";
    public static final String USER_PASS = "password123";
    public static final String USER_PASS_B64 = Base64.getEncoder().encodeToString(USER_PASS.getBytes());

    private TestDataFactory() {
    }

    public static UserModel aUser() {
        UserModel user = new UserModel();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        user.setLogin(USER_LOGIN);
        user.setPassword(USER_PASS);
        user.setActive(true);
        return user;
    }

    public static UserResponseDto aUserResponse() {
        return UserResponseDto.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .login(USER_LOGIN)
                .active(true)
                .build();
    }

    public static List<UserResponseDto> aUserResponseList() {
        return List.of(aUserResponse());
    }

    public static UserRequestDto aUserRequest() {
        return UserRequestDto.builder()
                .name("Updated Name")
                .login("updatedlogin")
                .password(USER_PASS_B64)
                .build();
    }

    public static List<UserModel> aUserList() {
        return List.of(aUser());
    }

    /** JSON body for POST /users with a valid Base64 password. */
    public static String registerJson() {
        return String.format(
                "{\"name\":\"%s\",\"login\":\"%s\",\"password\":\"%s\"}",
                USER_NAME, USER_LOGIN, USER_PASS_B64);
    }

    /** JSON body for PUT /users/{id}. */
    public static String updateJson() {
        return String.format(
                "{\"name\":\"Updated Name\",\"login\":\"updatedlogin\",\"password\":\"%s\"}",
                USER_PASS_B64);
    }
}
