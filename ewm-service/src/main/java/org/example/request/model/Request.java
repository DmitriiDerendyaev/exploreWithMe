package org.example.request.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.event.model.Event;
import org.example.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

import static org.example.constant.Constants.DATE_FORMAT;

@Data
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @JsonFormat(pattern = DATE_FORMAT)
    @Column(name = "created")
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(value = EnumType.STRING)
    private RequestStatus status;
}