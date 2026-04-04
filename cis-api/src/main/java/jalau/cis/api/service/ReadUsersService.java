package jalau.cis.api.service;

import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReadUsersService {

    @Autowired
    private UserRepository userRepository;

    public List<UserResponse> readAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse read(String id) {
        Optional<User> existing = userRepository.findById(id);
        return existing.map(this::mapToResponse).orElse(null);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getLogin(), user.getActive());
    }
}