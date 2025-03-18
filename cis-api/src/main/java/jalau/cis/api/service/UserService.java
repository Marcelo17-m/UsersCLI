package jalau.cis.api.service;

import jalau.cis.api.dto.UserResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse findById(String id) {
        return userRepository.findById(id)
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getName(),
                        u.getLogin()))
                .orElse(null);
    }

    public UserResponse findByLogin(String login) {
        return userRepository.findByLogin(login)
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getName(),
                        u.getLogin()))
                .orElse(null);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getName(),
                        u.getLogin()))
                .toList();
    }
}
