package org.example.user.mapper;

import org.example.user.dto.NewUserRequest;
import org.example.user.dto.UserDto;
import org.example.user.dto.UserShortDto;
import org.example.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUser(NewUserRequest newUserRequest) {
        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserShortDto toShort(User user) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(user.getId());
        userShortDto.setName(user.getName());
        return userShortDto;
    }
}
