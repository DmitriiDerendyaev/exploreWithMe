package org.example.user.service;

import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.user.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    public UserDto addUserAdmin(NewUserRequest newUserRequest);

    public List<UserDto> getUsersAdmin(List<Long> ids, Pageable pageable);

    public void deleteUserAdmin(Long id);
}
