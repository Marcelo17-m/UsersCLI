package jalau.cis.api.service;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
public class UpdateUserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserResponse update(String id, UserDto dto) {
        Optional<User> existing = userRepository.findById(id);

        if (existing.isEmpty()) {
            return null;
        }

        User user = existing.get();

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }
        if (dto.login() != null && !dto.login().isBlank()) {
            user.setLogin(dto.login());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(request.password());
                user.setPassword(new String(decodedBytes));
            } catch (IllegalArgumentException e) {
                user.setPassword(request.password());
            }
        }

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getLogin());
    }
}
