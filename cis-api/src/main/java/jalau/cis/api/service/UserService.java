package jalau.cis.api.service;

import jalau.cis.api.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserResponse findByLogin(String login) {
        String sql = "SELECT id, name, login FROM users WHERE login = ?";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper(), login);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private RowMapper<UserResponse> userRowMapper() {
        return (rs, rowNum) -> new UserResponse(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("login")
        );
    }
}
