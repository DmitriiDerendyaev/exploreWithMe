package org.example.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.StatsClient;
import org.example.StatsDto;
import org.example.category.model.Category;
import org.example.category.repository.CategoryRepository;
import org.example.event.dto.*;
import org.example.event.mapper.EventMapper;
import org.example.event.model.Event;
import org.example.event.model.EventSort;
import org.example.event.model.EventState;
import org.example.event.repository.EventRepository;
import org.example.exception.InvalidRequestException;
import org.example.exception.ObjectNotFoundException;
import org.example.exception.RulesViolationException;
import org.example.request.dto.EventRequestStatusUpdateRequest;
import org.example.request.dto.EventRequestStatusUpdateResult;
import org.example.request.dto.ParticipationRequestDto;
import org.example.request.mapper.RequestMapper;
import org.example.request.model.Request;
import org.example.request.model.RequestStatus;
import org.example.request.repository.RequestRepository;
import org.example.subscription.model.Subscription;
import org.example.subscription.repository.SubscriptionRepository;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.example.constant.Constants.DATE_FORMAT;
import static org.example.utils.HitsEventUtil.getHitsEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final StatsClient client;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new InvalidRequestException("Field: eventDate. " + "Error: Date must be after than now. Value:"
                    + newEventDto.getEventDate());
        }
        User user = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(newEventDto.getCategory());
        Event event = eventMapper.toEvent(newEventDto, category, user, null);
        event = eventRepository.save(event);

        log.info("Add new event");
        return eventMapper.toFull(event, getHitsEvent(event.getId(),
                LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client));
    }

    @Override
    public List<EventShortDto> getEventPrivate(Long userId, Pageable pageable) {
        log.info("Get owner events");
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(e -> eventMapper.toShort(e, getHitsEvent(e.getId(),
                        LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);
        log.info("Get information about event for owner");
        return eventMapper.toFull(event, getHitsEvent(event.getId(),
                LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client));
    }

    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        getEventOrThrow(eventId);
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new RulesViolationException("Only pending or canceled events can be changed");
        }
        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new InvalidRequestException("Field: eventDate. " + "Error: Date must be after than now. Value:"
                        + event.getEventDate());
            } else {
                event.setEventDate(updateEventUserRequest.getEventDate());
            }
        }
        if (updateEventUserRequest.getCategory() != null) {
            Long categoryId = updateEventUserRequest.getCategory();
            Category category = getCategoryOrThrow(categoryId);
            event.setCategory(category);
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getLocation() != null) {
            if (updateEventUserRequest.getLocation().getLat() != null) {
                event.setLat(updateEventUserRequest.getLocation().getLat());
            }
            if (updateEventUserRequest.getLocation().getLon() != null) {
                event.setLon(updateEventUserRequest.getLocation().getLon());
            }
        }
        updateEventParam(event, updateEventUserRequest.getPaid(), updateEventUserRequest.getParticipantLimit(), updateEventUserRequest.getRequestModeration(), updateEventUserRequest.getTitle());
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            } else {
                throw new InvalidRequestException("Unknown state, it must be: SEND_TO_REVIEW or CANCEL_REVIEW");
            }
        }
        log.info("Update Event");
        return eventMapper.toFull(eventRepository.save(event), 0L);
    }

    @Override
    public List<EventFullDto> getEventsAdmin(EventAdminParam eventAdminParam) {
        Specification<Event> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (eventAdminParam.getUsers() != null) {
                CriteriaBuilder.In<Long> usersInClause = criteriaBuilder.in(root.get("initiator"));
                for (Long user : eventAdminParam.getUsers()) {
                    usersInClause.value(user);
                }
                predicates.add(usersInClause);
            }

            if (eventAdminParam.getStates() != null) {
                EventState eventState;
                List<EventState> eventStates = new ArrayList<>();
                try {
                    for (String state : eventAdminParam.getStates()) {
                        eventState = EventState.valueOf(state);
                        eventStates.add(eventState);
                    }
                } catch (IllegalArgumentException e) {
                    throw new InvalidRequestException("Unknown parameter of state");
                }
                CriteriaBuilder.In<EventState> statesInClause = criteriaBuilder.in(root.get("state"));
                for (EventState st : eventStates) {
                    statesInClause.value(st);
                }
                predicates.add(statesInClause);
            }

            if (eventAdminParam.getCategories() != null) {
                CriteriaBuilder.In<Long> categoriesInClause = criteriaBuilder.in(root.get("category"));
                for (Long category : eventAdminParam.getCategories()) {
                    categoriesInClause.value(category);
                }
                predicates.add(categoriesInClause);
            }

            if (eventAdminParam.getRangeStart() != null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), eventAdminParam.getRangeStart()));
            }

            if (eventAdminParam.getRangeEnd() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), eventAdminParam.getRangeEnd()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        PageRequest pageable = PageRequest.of(eventAdminParam.getFrom() / eventAdminParam.getSize(), eventAdminParam.getSize(), Sort.by("id"));
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return events.stream().map(e -> eventMapper.toFull(e, getHitsEvent(e.getId(),
                        LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdmin) {
        Event event = getEventOrThrow(eventId);

        if (updateEventAdmin.getStateAction() != null) {
            if (updateEventAdmin.getStateAction().equals("PUBLISH_EVENT")) {
                if (!String.valueOf(event.getState()).equals("PENDING")) {
                    throw new RulesViolationException(
                            String.format("Event have state=%s but must have state PENDING", event.getState()));
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdmin.getStateAction().equals("REJECT_EVENT")) {
                if (String.valueOf(event.getState()).equals("PUBLISHED")) {
                    throw new RulesViolationException(
                            String.format("Event have state=%s adn can't be REJECT", event.getState()));
                }
                event.setState(EventState.CANCELED);
            } else {
                throw new RulesViolationException("StateAction must be PUBLISH_EVENT or REJECT_EVENT");
            }
        }

        if (updateEventAdmin.getAnnotation() != null) {
            event.setAnnotation(updateEventAdmin.getAnnotation());
        }
        if (updateEventAdmin.getCategory() != null && updateEventAdmin.getCategory() != 0) {
            Category category = getCategoryOrThrow(updateEventAdmin.getCategory());
            event.setCategory(category);
        }
        if (updateEventAdmin.getDescription() != null) {
            event.setDescription(updateEventAdmin.getDescription());
        }

        if (updateEventAdmin.getEventDate() != null) {
            if (updateEventAdmin.getEventDate().isBefore(LocalDateTime.now().minusHours(1))) {
                throw new InvalidRequestException("EventDate must be 1 hour earlier then time of publication");
            }
            event.setEventDate(updateEventAdmin.getEventDate());
        }
        if (updateEventAdmin.getLocation() != null) {
            event.setLon(updateEventAdmin.getLocation().getLon());
            event.setLat(updateEventAdmin.getLocation().getLat());
        }
        updateEventParam(event, updateEventAdmin.getPaid(), updateEventAdmin.getParticipantLimit(), updateEventAdmin.getRequestModeration(), updateEventAdmin.getTitle());
        event.setId(eventId);
        eventRepository.save(event);
        return eventMapper.toFull(event, getHitsEvent(eventId,
                LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client));
    }

    private void updateEventParam(Event event, Boolean paid, Integer participantLimit, Boolean requestModeration, String title) {
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null) {
            event.setTitle(title);
        }
    }

    @Override
    public List<EventShortDto> getEventsPublic(EventUserParam eventUserParam, HttpServletRequest request) {
        StatsDto statsDto = new StatsDto();
        statsDto.setIp(request.getRemoteAddr());
        statsDto.setUri(request.getRequestURI());
        statsDto.setApp("ewm-main-service");
        statsDto.setTimestamp(LocalDateTime.now());
        client.saveStats(statsDto);

        Specification<Event> specification = (((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventUserParam.getText() != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("annotation")), "%" + eventUserParam.getText().toLowerCase() + "%"),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")), "%" + eventUserParam.getText().toLowerCase() + "%")));
            }
            if (eventUserParam.getCategories() != null) {
                CriteriaBuilder.In<Long> categoriesInClause = criteriaBuilder.in(root.get("category"));
                for (Long category : eventUserParam.getCategories()) {
                    categoriesInClause.value(category);
                }
                predicates.add(categoriesInClause);
            }
            if (eventUserParam.getPaid() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), eventUserParam.getPaid()));
            }

            if (eventUserParam.getRangeStart() == null && eventUserParam.getRangeEnd() == null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            } else {

                if (eventUserParam.getRangeStart() != null &&
                        eventUserParam.getRangeEnd() != null &&
                        eventUserParam.getRangeStart().isAfter(eventUserParam.getRangeEnd())) {
                    throw new InvalidRequestException("rangeStart can't be after rangeEnd");
                } else if (eventUserParam.getRangeStart() != null) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), eventUserParam.getRangeStart()));
                } else {
                    predicates.add(criteriaBuilder.lessThan(root.get("eventDate"), eventUserParam.getRangeEnd()));
                }
            }

            if (eventUserParam.getOnlyAvailable() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("confirmedRequests"), root.get("participantLimit")));
            }
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }));

        if (eventUserParam.getSort() == null) {
            Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(), eventUserParam.getSize(), Sort.by("id"));
            return getOutputEventsStream(specification, pageable);

        } else if (eventUserParam.getSort().equals(String.valueOf(EventSort.EVENT_DATE))) {
            Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(), eventUserParam.getSize(), Sort.by("eventDate"));
            return getOutputEventsStream(specification, pageable);

        } else if (eventUserParam.getSort().equals(String.valueOf(EventSort.VIEWS))) {
            Pageable pageable = PageRequest.of(eventUserParam.getFrom() / eventUserParam.getSize(), eventUserParam.getSize(), Sort.unsorted());
            return getOutputEventsStream(specification, pageable).stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews))
                    .collect(Collectors.toList());
        }
        throw new InvalidRequestException("Sort cat be EVENT_DATE or VIEWS");
    }

    public List<EventShortDto> getOutputEventsStream(Specification<Event> specification, Pageable pageable) {
        List<Event> allEvents = eventRepository.findAll(specification, pageable).getContent();
        return allEvents.stream()
                .map(r -> eventMapper.toShort(r, getHitsEvent(r.getId(),
                        LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByIdPublic(Long id, HttpServletRequest request) {
        StatsDto statsDto = new StatsDto();
        statsDto.setIp(request.getRemoteAddr());
        statsDto.setUri(request.getRequestURI());
        statsDto.setApp("ewm-main-service");
        statsDto.setTimestamp(LocalDateTime.now());
        client.saveStats(statsDto);

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new ObjectNotFoundException(String.format("Event with id=%d was not found", id)));

        Long view = getHitsEvent(id, LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), true, client);

        return eventMapper.toFull(event, view);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsUserToEventPrivate(Long userId, Long eventId) {
        getEventOrThrow(eventId);
        getUserOrThrow(userId);
        log.info("All requests to event");
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
            EventRequestStatusUpdateResult updateResult;
            List<Request> confirmedRequests = new ArrayList<>();
            List<Request> rejectedRequests = new ArrayList<>();
            int countRequests = updateRequest.getRequestIds().size();
            List<Request> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());
            getUserOrThrow(userId);
            Event event = getEventOrThrow(eventId);

            if (!event.getInitiator().getId().equals(userId)) {
                throw new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId));
            }
            for (Request request : requests) {
                if (!request.getStatus().equals(RequestStatus.PENDING)) {
                    throw new RulesViolationException("Request status is not PENDING");
                }
            }
            if (updateRequest.getStatus() != null) {
                switch (updateRequest.getStatus()) {
                    case "CONFIRMED":
                        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()
                                || event.getParticipantLimit() > event.getConfirmedRequests() + countRequests) {
                            requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
                            event.setConfirmedRequests(event.getConfirmedRequests() + countRequests);
                            confirmedRequests.addAll(requests);

                        } else if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                            throw new RulesViolationException("Participant Limit");
                        } else {
                            for (Request request : requests) {
                                if (event.getParticipantLimit() > event.getConfirmedRequests()) {
                                    request.setStatus(RequestStatus.CONFIRMED);
                                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                                    confirmedRequests.add(request);
                                } else {
                                    request.setStatus(RequestStatus.REJECTED);
                                    rejectedRequests.add(request);
                                }
                            }
                        }
                        break;
                    case "REJECTED":
                        requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
                        rejectedRequests.addAll(requests);
                }
            }
            eventRepository.save(event);
            requestRepository.saveAll(requests);

            List<ParticipationRequestDto> confirmed = confirmedRequests.stream()
                    .map(requestMapper::toDto)
                    .collect(Collectors.toList());
            List<ParticipationRequestDto> rejected = rejectedRequests.stream()
                    .map(requestMapper::toDto)
                    .collect(Collectors.toList());
            updateResult = new EventRequestStatusUpdateResult(confirmed, rejected);
            log.info(String.format("Update states (%s) of event's (id=%d) requests.", updateRequest.getStatus(), eventId));
            return updateResult;
    }

    @Override
    public List<EventFullDto> findEventsByUser(Long userId, Long authorId, Pageable pageable) {
        if (Objects.equals(userId, authorId)) {
            throw new RulesViolationException("User can't be subscribe to himself");
        }
        Subscription subscription = subscriptionRepository.findBySubscriberIdAndSubscribedToId(userId, authorId);

        if (subscription == null) {
            throw new ObjectNotFoundException("Subscribe not found");
        }

        List<Event> events = eventRepository.findByInitiatorIdAndState(authorId, EventState.PUBLISHED, pageable);
        log.info("Events found for user with id={} from subscriber with id={}", userId, authorId);
        return events.stream().map(e -> eventMapper.toFull(e, getHitsEvent(e.getId(),
                        LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> findEventsByAllUsers(Long userId, Pageable pageable) {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(userId, pageable);
        List<Long> usersIds = subscriptions.stream().map(subscription -> subscription.getSubscribedTo().getId()).collect(Collectors.toList());

        if (usersIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Event> events = eventRepository.findByStateAndInitiatorIdIn(EventState.PUBLISHED, usersIds, pageable);

        log.info("Найдены все события от пользователей для подписчика с id={}", userId);
        return events.stream().map(e -> eventMapper.toShort(e, getHitsEvent(e.getId(),
                        LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), false, client)))
                .collect(Collectors.toList());
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new ObjectNotFoundException(
                        String.format("Category with id=%d was not found", categoryId)));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%d not found", userId)));
    }

}
