package org.example.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.example.category.dto.CategoryDto;
import org.example.event.model.Location;
import org.example.user.dto.UserShortDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

import static org.example.constant.Constants.DATE_FORMAT;

@Getter
@Setter
public class EventFullDto {
    private Long id;

    @NotNull
    private String annotation;

    @NotNull
    private CategoryDto category;

    private Integer confirmedRequests;

    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDateTime createdOn;

    private String description;

    @NotNull
    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDateTime eventDate;

    @NotNull
    private UserShortDto initiator;

    @NotNull
    private Location location;

    @NotNull
    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private String state;

    @NotNull
    private String title;

    private Long views;
}