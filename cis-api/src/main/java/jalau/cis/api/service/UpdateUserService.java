package jalau.cis.api.service;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
public class UpdateUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(dto.password());
                user.setPassword(new String(decodedBytes));
            } catch (IllegalArgumentException e) {
                user.setPassword(dto.password()); // fallback to plain if not base64
            }
        }

        String sql = "UPDATE users SET name = ?, login = ?, password = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getPassword(), id);

        return user;
    }
}
