package org.example.event.repository;

import org.example.event.model.Event;
import org.example.event.model.EventSort;
import org.example.event.model.EventState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Event findByInitiatorIdAndId(Long userId, Long eventId);

    List<Event> findByIdIn(List<Long> ids);

    Event findAllByCategoryId(Long catId);

    List<Event> findAllByIdIn(List<Long> ids);

    Optional<Event> findByIdAndState(Long id, EventState state);
}
