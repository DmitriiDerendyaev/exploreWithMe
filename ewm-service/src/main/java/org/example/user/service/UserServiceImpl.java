package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.ObjectAlreadyExistException;
import org.example.exception.ObjectNotFoundException;
import org.example.exception.RulesViolationException;
import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.user.dto.UserWithSubscribers;
import org.example.user.mapper.UserMapper;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public UserWithSubscribers addSubscriber(Long userId, Long authorId) {
        if (userId.equals(authorId)) {
            throw new RulesViolationException("The user cannot subscribe to himself");
        }
        User user = getUserOrThrow(userId);
        User subscriber = getUserOrThrow(authorId);
        if (user.getSubscriptions().contains(subscriber)) {
            throw new RulesViolationException("You subscribe on this user");
        }
        user.getSubscriptions().add(subscriber);
        user = userRepository.save(user);
        System.out.println(user);
        log.info("Add user subscriber with id={}", authorId);
        return userMapper.toUserDtoWithSubscribers(user);
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long userId, Long authorId) {
        if (userId.equals(authorId)) {
            throw new RulesViolationException("User can't subscribe yourself");
        }
        User user = getUserOrThrow(userId);
        User subscriber = getUserOrThrow(authorId);
        if (!user.getSubscriptions().contains(subscriber)) {
            throw new ObjectNotFoundException("User with id=" + userId + "not subscribe on user id=" + authorId);
        }
        user.getSubscriptions().remove(subscriber);
        log.info("User id={} unsubscribed from user id={}", userId, authorId);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserWithSubscribers getUserWithSubscribers(Long userId, Pageable pageable) {
        User user = getUserOrThrow(userId);
        return userMapper.toUserDtoWithSubscribers(user);
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%d not found", userId)));
    }
}
