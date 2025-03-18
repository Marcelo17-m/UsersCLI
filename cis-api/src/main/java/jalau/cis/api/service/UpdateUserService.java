package jalau.cis.api.service;

import jalau.cis.api.dto.UserDto;
import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateUserService {

    @Autowired
    private UserRepository userRepository;

    public User update(String id, UserDto dto){
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
            user.setPassword(dto.password());
        }

        return userRepository.save(user);
    }
}
