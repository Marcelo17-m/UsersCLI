package jalau.cis.api.util;

import jalau.cis.api.dto.UserUpdateRequest;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;

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

    public static User aUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        user.setLogin(USER_LOGIN);
        user.setPassword(USER_PASS);
        return user;
    }

    public static UserResponse aUserResponse() {
        return new UserResponse(USER_ID, USER_NAME, USER_LOGIN, true);
    }

    public static UserUpdateRequest aUserUpdateRequest() {
        return new UserUpdateRequest("Updated Name", "updatedlogin", USER_PASS_B64);
    }

    public static List<User> aUserList() {
        return List.of(aUser());
    }

    public static List<UserResponse> aUserResponseList() {
        return List.of(aUserResponse());
    }

    public static String registerJson() {
        return String.format(
                "{\"name\":\"%s\",\"login\":\"%s\",\"password\":\"%s\"}",
                USER_NAME, USER_LOGIN, USER_PASS_B64);
    }

    public static String updateJson() {
        return String.format(
                "{\"name\":\"Updated Name\",\"login\":\"updatedlogin\",\"password\":\"%s\"}",
                USER_PASS_B64);
    }
}
