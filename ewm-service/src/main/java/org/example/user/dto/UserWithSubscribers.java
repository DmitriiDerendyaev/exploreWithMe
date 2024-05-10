package org.example.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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