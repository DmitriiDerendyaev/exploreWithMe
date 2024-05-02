package org.example.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.model.Event;
import org.example.event.model.EventState;
import org.example.event.repository.EventRepository;
import org.example.exceprtion.ObjectNotFoundException;
import org.example.exceprtion.RulesViolationException;
import org.example.request.dto.ParticipationRequestDto;
import org.example.request.mapper.RequestMapper;
import org.example.request.model.Request;
import org.example.request.model.RequestStatus;
import org.example.request.repository.RequestRepository;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;
    @Override
    public ParticipationRequestDto addRequestPrivate(Long userId, Long eventId) {
        Request request = new Request();
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId)));
        User user = getUserOrThrow(userId);
        List<Request> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        if (!requests.isEmpty()) {
            throw new RulesViolationException("Your request added");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new RulesViolationException("Event's owner can't be add");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RulesViolationException("Can't add in not published event");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new RulesViolationException("Participant Limit");
        }

        request.setRequester(user);
        request.setEvent(event);
        request.setStatus(RequestStatus.PENDING);
        request.setCreated(LocalDateTime.now());

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("Add request by user");
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsPrivate(Long userId) {
        getUserOrThrow(userId);
        log.info("Get user requests to event");
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto).collect(Collectors.toList());    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUserOrThrow(userId);
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Request with id=%d not found", requestId)));
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        log.info("Canceled request");
        return requestMapper.toDto(request);
    }

    private User getUserOrThrow(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%d not found", id)));
    }
}
