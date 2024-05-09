package org.example.request.mapper;

import org.example.request.dto.ParticipationRequestDto;
import org.example.request.model.Request;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(Request request) {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(request.getId());
        participationRequestDto.setRequester(request.getRequester().getId());
        participationRequestDto.setEvent(request.getEvent().getId());
        participationRequestDto.setCreated(request.getCreated());
        participationRequestDto.setStatus(String.valueOf(request.getStatus()));
        return participationRequestDto;
    }
}
