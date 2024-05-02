package org.example.compilation.dto;


import lombok.Getter;
import lombok.Setter;
import org.example.event.dto.EventShortDto;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class CompilationDto {
    private Long id;

    private List<EventShortDto> events;

    private Boolean pinned;

    @NotBlank
    private String title;
}