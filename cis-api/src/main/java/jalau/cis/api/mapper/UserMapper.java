package jalau.cis.api.mapper;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.model.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(UserModel user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .active(user.getActive())
                .build();
    }

    public UserResponseDto toCreatedResponseDto(UserModel user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .message("User registered successfully")
                .build();
    }

    public UserModel toNewModel(UserRequestDto request, String id, String plainPassword) {
        if (request == null) {
            return null;
        }

        UserModel user = new UserModel();
        user.setId(id);
        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setPassword(plainPassword);
        user.setActive(true);
        return user;
    }

    public void updateModel(UserModel user, UserRequestDto request, String decodedPassword) {
        if (user == null || request == null) {
            return;
        }

        if (hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (hasText(request.getLogin())) {
            user.setLogin(request.getLogin());
        }
        if (decodedPassword != null) {
            user.setPassword(decodedPassword);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
