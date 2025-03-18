package jalau.cis.api.service;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
public class UpdateUserService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UpdateUserService(UserRepository userRepository,
                             JdbcTemplate jdbcTemplate,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User update(String id, UserDto dto){
        System.out.println("Updating user: " + id + " with data: " + dto);
        Optional<User> existing = userRepository.findById(id);

        if(existing.isEmpty()){
            return null;
        }

        User user = existing.get();

        if (dto.name() != null){
            user.setName(dto.name());
        }
        if (dto.login() != null){
            user.setLogin(dto.login());
        }
        if (dto.password() != null){
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        String sql = "UPDATE users SET name = ?, login = ?, password = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getPassword(), id);

        return user;
    }
}
