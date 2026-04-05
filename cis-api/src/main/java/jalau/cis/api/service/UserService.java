package jalau.cis.api.service;

import jalau.cis.api.dto.UserRequestDto;
import jalau.cis.api.dto.UserResponseDto;
import jalau.cis.api.exception.DuplicateLoginException;
import jalau.cis.api.exception.UserNotFoundException;
import jalau.cis.api.mapper.UserMapper;
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
        private final UserMapper userMapper;

        @Autowired
        public UserService(UserRepository userRepository, UserMapper userMapper) {
                this.userRepository = userRepository;
                this.userMapper = userMapper;
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

                String plainPassword;
                try {
                        byte[] decodedBytes = Base64.getDecoder().decode(dto.getPassword());
                        plainPassword = new String(decodedBytes);
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid format. Password must be Base64.");
                }

                UserModel user = userMapper.toNewModel(dto, UUID.randomUUID().toString(), plainPassword);

                UserModel saved = userRepository.save(user);
                return userMapper.toCreatedResponseDto(saved);
        }

        @Transactional
        public UserResponseDto update(String id, UserRequestDto dto) {
                UserModel user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found."));

                String decodedPassword = null;
                if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                        try {
                                byte[] decodedBytes = Base64.getDecoder().decode(dto.getPassword());
                                decodedPassword = new String(decodedBytes);
                        } catch (IllegalArgumentException e) {
                                decodedPassword = dto.getPassword();
                        }
                }

                userMapper.updateModel(user, dto, decodedPassword);

                UserModel saved = userRepository.save(user);
                return userMapper.toResponseDto(saved);
        }

        public boolean delete(String id) {
                UserModel user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found."));
                user.setActive(false);
                userRepository.save(user);

                return true;
        }
}
