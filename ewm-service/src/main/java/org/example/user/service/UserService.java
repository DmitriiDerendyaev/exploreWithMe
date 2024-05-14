package org.example.user.service;

import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.subscription.dto.UserWithSubscribers;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface UserService {

    UserDto addUserAdmin(NewUserRequest newUserRequest);

    List<UserDto> getUsersAdmin(List<Long> ids, Pageable pageable);

    void deleteUserAdmin(@PathVariable Long id);

    UserWithSubscribers addSubscriber(Long userId, Long authorId);

    void deleteSubscriber(Long userId, Long authorId);

    UserWithSubscribers getUserWithSubscribers(Long userId, Pageable pageable);
}
