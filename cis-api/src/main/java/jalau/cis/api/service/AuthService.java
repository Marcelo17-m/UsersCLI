package jalau.cis.api.service;

import jalau.cis.api.dto.AuthRequestDto;
import jalau.cis.api.dto.AuthResponseDto;
import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.exception.DuplicateLoginException;
import jalau.cis.api.exception.InactiveUserException;
import jalau.cis.api.exception.InvalidCredentialsException;
import jalau.cis.api.model.UserModel;
import jalau.cis.api.repository.UserRepository;
import jalau.cis.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto login(AuthRequestDto request) {
        UserModel user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Credentials"));

        if (user.getActive() == null || !user.getActive()) {
            throw new InactiveUserException("Inactive User");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user.getLogin());
        return new AuthResponseDto(token, "Successful login");
    }

    public void register(UserRequestDto request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new DuplicateLoginException("User with same Login already exists.");
        }

        UserModel user = new UserModel();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        userRepository.save(user);
    }
}
