package org.example.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserDto {
    private Long id;

    @Email
    @NotBlank
    @Size(min = 6, max = 254, message = "Email length can be 6 to 254")
    private String email;

    @NotBlank
    @Size(min = 2, max = 250, message = "Name can be 2 to 250")
    private String name;
}