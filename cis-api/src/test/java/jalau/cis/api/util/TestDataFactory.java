package jalau.cis.api.util;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;

import java.util.Base64;
import java.util.List;

public final class TestDataFactory {

    public static final String USER_ID    = "test-id-001";
    public static final String USER_NAME  = "Test User";
    public static final String USER_LOGIN = "testlogin";
    public static final String USER_PASS  = "password123";
    public static final String USER_PASS_B64 =
            Base64.getEncoder().encodeToString(USER_PASS.getBytes());

    private TestDataFactory() {}

    public static User aUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setName(USER_NAME);
        user.setLogin(USER_LOGIN);
        user.setPassword(USER_PASS);
        return user;
    }

    public static UserResponse aUserResponse() {
        return new UserResponse(USER_ID, USER_NAME, USER_LOGIN);
    }

    public static UserDto aUserDto() {
        return new UserDto("Updated Name", "updatedlogin", USER_PASS_B64);
    }

    public static List<User> aUserList() {
        return List.of(aUser());
    }

    /** JSON body for POST /users with a valid Base64 password. */
    public static String registerJson() {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"login\":\"%s\",\"password\":\"%s\"}",
                USER_ID, USER_NAME, USER_LOGIN, USER_PASS_B64);
    }

    /** JSON body for PUT /users/{id}. */
    public static String updateJson() {
        return String.format(
                "{\"name\":\"Updated Name\",\"login\":\"updatedlogin\",\"password\":\"%s\"}",
                USER_PASS_B64);
    }
}
