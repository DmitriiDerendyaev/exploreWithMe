package org.example.user.service;

import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface UserService {

    public UserDto addUserAdmin(NewUserRequest newUserRequest);

    public List<UserDto> getUsersAdmin(List<Long> ids, Pageable pageable);

    public void deleteUserAdmin(@PathVariable Long id);
}
