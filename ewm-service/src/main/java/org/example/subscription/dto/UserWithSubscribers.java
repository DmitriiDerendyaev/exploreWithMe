package org.example.subscription.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.user.dto.UserDto;

import java.util.List;

@Getter
@Setter
@Builder
public class UserWithSubscribers {
    private Long id;
    private String name;
    private String email;
    private List<UserDto> subscribers;
}