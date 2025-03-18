package jalau.cis.api.service;

import jalau.cis.api.dto.LoginRequest;
import jalau.cis.api.dto.LoginResponse;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import jalau.cis.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (user.getActive() == null || !user.getActive()) {
            throw new RuntimeException("Inactive User");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user.getLogin());
        return new LoginResponse(token, "Successful login");
    }
}
