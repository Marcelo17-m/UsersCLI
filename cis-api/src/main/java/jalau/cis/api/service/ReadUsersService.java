package jalau.cis.api.service;

import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReadUsersService {

    @Autowired
    private UserRepository userRepository;

    public List<User> readAll() {
        return userRepository.findAll();
    }

    public User read(String id) {
        Optional<User> existing = userRepository.findById(id);
        return existing.orElse(null);
    }
}