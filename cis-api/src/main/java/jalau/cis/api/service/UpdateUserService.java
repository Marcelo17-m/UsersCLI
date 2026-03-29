package jalau.cis.api.service;

import jalau.cis.api.dto.UserUpdateRequest;
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
    public User update(String id, UserUpdateRequest request) {
        System.out.println("Updating user: " + id + " with data: " + request);
        Optional<User> existing = userRepository.findById(id);

        if(existing.isEmpty()){
            return null;
        }

        User user = existing.get();

        if (request.name() != null){
            user.setName(request.name());
        }
        if (request.login() != null){
            user.setLogin(request.login());
        }
        if (request.password() != null){
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(request.password());
                user.setPassword(new String(decodedBytes));
            } catch (IllegalArgumentException e) {
                user.setPassword(request.password());
            }
        }

        String sql = "UPDATE users SET name = ?, login = ?, password = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getLogin(), user.getPassword(), id);

        return user;
    }
}
