package jalau.cis.api.service;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.exception.DuplicateLoginException;
import jalau.cis.api.exception.UserNotFoundException;
import jalau.cis.api.mapper.UserMapper;
import jalau.cis.api.model.UserModel;
import jalau.cis.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;

        @Autowired
        public UserService(UserRepository userRepository, UserMapper userMapper,  PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.userMapper = userMapper;
                this.passwordEncoder = passwordEncoder;
        }

        public UserResponseDto findById(String id) {
                return userRepository.findById(id)
                                .map(userMapper::toResponseDto)
                                .orElseThrow(() -> new UserNotFoundException("User not found."));
        }

        public UserResponseDto findByLogin(String login) {
                return userRepository.findByLogin(login)
                                .map(userMapper::toResponseDto)
                                .orElseThrow(() -> new UserNotFoundException("User not found."));
        }

        public List<UserResponseDto> findAll() {
                return userRepository.findAll().stream()
                                .map(userMapper::toResponseDto)
                                .toList();
        }

        public UserResponseDto create(UserRequestDto dto) {
                if (userRepository.findByLogin(dto.getLogin()).isPresent()) {
                        throw new DuplicateLoginException("User with same Login already exists.");
                }

                String hashedpassword = passwordEncoder.encode(dto.getPassword());

                UserModel user = userMapper.toNewModel(dto, UUID.randomUUID().toString(), hashedpassword);

                UserModel saved = userRepository.save(user);
                return userMapper.toCreatedResponseDto(saved);
        }

        @Transactional
        public UserResponseDto update(String id, UserRequestDto dto) {
                UserModel user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found."));

                if (dto.getLogin() != null && !dto.getLogin().isBlank()
                        && !dto.getLogin().equals(user.getLogin())) {
                    if (userRepository.findByLogin(dto.getLogin()).isPresent()) {
                        throw new DuplicateLoginException("User with same Login already exists.");
                    }
                }


                String hashedPassword = null;
                if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                    hashedPassword = passwordEncoder.encode(dto.getPassword());
                }

                userMapper.updateModel(user, dto, hashedPassword);

                UserModel saved = userRepository.save(user);
                return userMapper.toResponseDto(saved);
        }

        public boolean delete(String id, String authenticatedLogin) {
                UserModel user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found."));

                if (!user.getLogin().equals(authenticatedLogin)) {
                    throw new AccessDeniedException("You are not authorized to perform this action.");
                }
                user.setActive(false);
                userRepository.save(user);

                return true;
        }
}
