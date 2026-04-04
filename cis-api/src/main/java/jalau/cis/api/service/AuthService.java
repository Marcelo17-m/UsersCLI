package jalau.cis.api.service;

import jalau.cis.api.dto.AuthRequestDto;
import jalau.cis.api.dto.AuthResponseDto;
import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.model.UserModel;
import jalau.cis.api.repository.UserRepository;
import jalau.cis.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDto login(AuthRequestDto request) {
        UserModel user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (user.getActive() == null || !user.getActive()) {
            throw new RuntimeException("Inactive User");
        }

        String plainPassword;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(request.getPassword());
            plainPassword = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Credentials");
        }

        if (!plainPassword.equals(user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user.getLogin());
        return new AuthResponseDto(token, "Successful login");
    }

    public void register(UserRequestDto request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new IllegalStateException("User with same Login already exists.");
        }

        String plainPassword;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(request.getPassword());
            plainPassword = new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format. Password must be Base64.");
        }

        UserModel user = new UserModel();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setPassword(plainPassword);
        user.setActive(true);
        userRepository.save(user);
    }
}
