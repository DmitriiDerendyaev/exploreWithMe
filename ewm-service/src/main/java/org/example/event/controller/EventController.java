package org.example.event.controller;

import lombok.RequiredArgsConstructor;
import org.example.event.dto.*;
import org.example.event.service.EventService;
import org.example.request.dto.EventRequestStatusUpdateRequest;
import org.example.request.dto.EventRequestStatusUpdateResult;
import org.example.request.dto.ParticipationRequestDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

import static org.example.constant.Constants.DATE_FORMAT;

@RestController
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEventPrivate(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEventPrivate(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventPrivate(@PathVariable Long userId,
                                               @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                               @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return eventService.getEventPrivate(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdPrivate(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        return eventService.getEventByIdPrivate(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventPrivate(@PathVariable Long userId, @PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventUserRequest updateEvent) {
        return eventService.updateEventPrivate(userId, eventId, updateEvent);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsUserToEventPrivate(@PathVariable Long userId,
                                                                       @PathVariable Long eventId) {
        return eventService.getRequestsUserToEventPrivate(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateEventRequestStatusPrivate(@PathVariable Long userId,
                                                                          @PathVariable Long eventId,
                                                                          @Valid @RequestBody EventRequestStatusUpdateRequest updateRequests) {
        return eventService.updateEventRequestStatusPrivate(userId, eventId, updateRequests);
    }

    @GetMapping("/admin/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsAdmin(@RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<String> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeStart,
                                             @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeEnd,
                                             @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                             @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        EventAdminParam eventAdminParam = new EventAdminParam();
        eventAdminParam.setUsers(users);
        eventAdminParam.setStates(states);
        eventAdminParam.setCategories(categories);
        eventAdminParam.setRangeStart(rangeStart);
        eventAdminParam.setRangeEnd(rangeEnd);
        eventAdminParam.setFrom(from);
        eventAdminParam.setSize(size);

        return eventService.getEventsAdmin(eventAdminParam);
    }

    @PatchMapping("/admin/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventAdmin(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest updateEventAdmin) {
        return eventService.updateEventAdmin(eventId, updateEventAdmin);
    }

    @GetMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsPublic(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {

        EventUserParam eventUserParam = new EventUserParam();
        eventUserParam.setText(text);
        eventUserParam.setCategories(categories);
        eventUserParam.setPaid(paid);
        eventUserParam.setRangeStart(rangeStart);
        eventUserParam.setRangeEnd(rangeEnd);
        eventUserParam.setOnlyAvailable(onlyAvailable);
        eventUserParam.setSort(sort);
        eventUserParam.setFrom(from);
        eventUserParam.setSize(size);

        return eventService.getEventsPublic(eventUserParam, request);
    }

    @GetMapping("/events/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdPublic(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEventByIdPublic(id, request);
    }

    @GetMapping("/users/{userId}/subscriptions/{authorId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> findEventsByUser(@PathVariable Long userId,
                                               @PathVariable Long authorId,
                                               @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                               @RequestParam(defaultValue = "10") @Min(1) Integer size) {

        return eventService.findEventsByUser(userId, authorId, PageRequest.of(from / size, size));
    }

    @GetMapping("/users/subscriptions/{userId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> findEventsByAllUsers(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                    @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        return eventService.findEventsByAllUsers(userId, PageRequest.of(from / size, size));
    }
}