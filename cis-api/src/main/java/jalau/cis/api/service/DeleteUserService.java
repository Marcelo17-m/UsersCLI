package jalau.cis.api.service;

import jalau.cis.api.model.User;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeleteUserService {

    @Autowired
    private UserRepository userRepository;

    public boolean delete(String id){
        Optional<User> existing = userRepository.findById(id);

        if (existing.isEmpty()){
            return false;
        }

        User user = existing.get();
        user.setActive(false);
        userRepository.save(user);

        return true;
    }
}
