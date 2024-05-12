package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.ObjectAlreadyExistException;
import org.example.exception.ObjectNotFoundException;
import org.example.exception.RulesViolationException;
import org.example.subscription.model.Subscription;
import org.example.subscription.repository.SubscriptionRepository;
import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.subscription.dto.UserWithSubscribers;
import org.example.user.mapper.UserMapper;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SubscriptionRepository subscriptionRepository;

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
    @Transactional
    public UserWithSubscribers addSubscriber(Long userId, Long authorId) {
        if (userId.equals(authorId)) {
            throw new RulesViolationException("The user cannot subscribe to himself");
        }
        User user = getUserOrThrow(userId);
        User author = getUserOrThrow(authorId);

        if (subscriptionRepository.existsBySubscriberAndSubscribedTo(user, author)) {
            throw new RulesViolationException("You subscribe on this user");
        }

        Subscription subscription = new Subscription();
        subscription.setSubscriber(user);
        subscription.setSubscribedTo(author);
        subscriptionRepository.save(subscription);
        log.info("User with id={} subscribed on user with id={}", userId, authorId);
        return userMapper.toUserDtoWithSubscribers(user, Collections.singletonList(author));
    }

    @Override
    @Transactional
    public void deleteSubscriber(Long userId, Long authorId) {
        if (userId.equals(authorId)) {
            throw new RulesViolationException("User can't unsubscribe yourself");
        }

        Subscription subscription = subscriptionRepository.findBySubscriberIdAndSubscribedToId(userId, authorId);

        if (subscription == null) {
            throw new ObjectNotFoundException("Subscription not found");
        }

        subscriptionRepository.delete(subscription);
        log.info("User with id={} unsubscribed from user with id={}", userId, authorId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserWithSubscribers getUserWithSubscribers(Long userId, Pageable pageable) {
        User user = getUserOrThrow(userId);
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(userId, pageable);
        List<User> subscribers = subscriptions.stream().map(Subscription::getSubscribedTo).collect(Collectors.toList());
        return userMapper.toUserDtoWithSubscribers(user, subscribers);
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%d not found", userId)));
    }
}
