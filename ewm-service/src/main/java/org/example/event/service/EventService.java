package org.example.event.service;

import org.example.event.dto.*;
import org.example.request.dto.EventRequestStatusUpdateRequest;
import org.example.request.dto.EventRequestStatusUpdateResult;
import org.example.request.dto.ParticipationRequestDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventPrivate(Long userId, Pageable pageable);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> getEventsAdmin(EventAdminParam eventAdminParam);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdmin);

    List<EventShortDto> getEventsPublic(EventUserParam eventUserParam, HttpServletRequest request);

    EventFullDto getEventByIdPublic(Long id, HttpServletRequest request);

    List<ParticipationRequestDto> getRequestsUserToEventPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequests);

    List<EventFullDto> findEventsByUser(Long userId, Long authorId, Pageable pageable);

    List<EventShortDto> findEventsByAllUsers(Long userId, Pageable pageable);
}
