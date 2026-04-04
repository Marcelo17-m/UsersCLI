package jalau.cis.api.service;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.model.UserModel;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

        private final UserRepository userRepository;

        @Autowired
        public UserService(UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        public UserResponseDto findById(String id) {
                return userRepository.findById(id)
                                .map(u -> UserResponseDto.builder()
                                                .id(u.getId())
                                                .name(u.getName())
                                                .login(u.getLogin())
                                                .active(u.getActive())
                                                .build())
                                .orElse(null);
        }

        public UserResponseDto findByLogin(String login) {
                return userRepository.findByLogin(login)
                                .map(u -> UserResponseDto.builder()
                                                .id(u.getId())
                                                .name(u.getName())
                                                .login(u.getLogin())
                                                .active(u.getActive())
                                                .build())
                                .orElse(null);
        }

        public List<UserResponseDto> findAll() {
                return userRepository.findAll().stream()
                                .map(u -> UserResponseDto.builder()
                                                .id(u.getId())
                                                .name(u.getName())
                                                .login(u.getLogin())
                                                .active(u.getActive())
                                                .build())
                                .toList();
        }

        public UserResponseDto create(UserRequestDto dto) {
                if (userRepository.findByLogin(dto.getLogin()).isPresent()) {
                        throw new IllegalStateException("User with same Login already exists.");
                }

                String plainPassword;
                try {
                        byte[] decodedBytes = Base64.getDecoder().decode(dto.getPassword());
                        plainPassword = new String(decodedBytes);
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid format. Password must be Base64.");
                }

                UserModel user = new UserModel();
                user.setId(UUID.randomUUID().toString());
                user.setName(dto.getName());
                user.setLogin(dto.getLogin());
                user.setPassword(plainPassword);
                user.setActive(true);

                UserModel saved = userRepository.save(user);
                return UserResponseDto.builder()
                                .id(saved.getId())
                                .message("User registered successfully")
                                .build();
        }

        @Transactional
        public UserResponseDto update(String id, UserRequestDto dto) {
                Optional<UserModel> existing = userRepository.findById(id);

                if (existing.isEmpty()) {
                        return null;
                }

                UserModel user = existing.get();

                if (dto.getName() != null && !dto.getName().isBlank()) {
                        user.setName(dto.getName());
                }
                if (dto.getLogin() != null && !dto.getLogin().isBlank()) {
                        user.setLogin(dto.getLogin());
                }
                if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                        try {
                                byte[] decodedBytes = Base64.getDecoder().decode(dto.getPassword());
                                user.setPassword(new String(decodedBytes));
                        } catch (IllegalArgumentException e) {
                                user.setPassword(dto.getPassword());
                        }
                }

                UserModel saved = userRepository.save(user);
                return UserResponseDto.builder()
                        .id(saved.getId())
                        .name(saved.getName())
                        .login(saved.getLogin())
                        .active(saved.getActive())
                        .build();
        }

        public boolean delete(String id) {
                Optional<UserModel> existing = userRepository.findById(id);

                if (existing.isEmpty()) {
                        return false;
                }

                UserModel user = existing.get();
                user.setActive(false);
                userRepository.save(user);

                return true;
        }
}
