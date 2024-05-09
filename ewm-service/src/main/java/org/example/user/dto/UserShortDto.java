package org.example.user.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserShortDto {
    @NotNull
    private Long id;

    @NotNull
    private String name;
}