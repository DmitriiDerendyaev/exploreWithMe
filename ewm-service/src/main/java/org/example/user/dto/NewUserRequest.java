package org.example.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
public class NewUserRequest {
    private String email;
    private String name;
}
