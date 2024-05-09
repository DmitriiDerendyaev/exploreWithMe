package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.ObjectAlreadyExistException;
import org.example.exception.ObjectNotFoundException;
import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.user.mapper.UserMapper;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUserAdmin(NewUserRequest newUserRequest) {
        if (userRepository.existsUserByEmail(newUserRequest.getEmail())) {
            throw new ObjectAlreadyExistException(String.format("User with Email: %s already exist", newUserRequest.getEmail()));
        }
        log.info("Add new user: {}", newUserRequest.toString());
        return userMapper.toDto(userRepository.save(userMapper.toUser(newUserRequest)));
    }

    @Override
    public List<UserDto> getUsersAdmin(List<Long> ids, Pageable pageable) {
        log.info("Get users by ids list: {}", ids);
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findByIdIn(ids).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserAdmin(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException(String.format("User with ID=%d NOT FOUND", id));
        }
        userRepository.deleteById(id);
        log.info("Delete user by ID: {}", id);
    }
}
