package org.example.request.repository;

import org.example.request.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterIdAndEventId(long eventId, long userId);

    List<Request> findAllByRequesterId(long userId);

    List<Request> findAllByEventId(long eventId);

    List<Request> findByIdIn(List<Long> ids);
}
