package org.example.mapper;

import lombok.RequiredArgsConstructor;
import org.example.StatsDto;
import org.example.StatsDtoOutput;
import org.example.model.Stats;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsMapper {

    public Stats toStats(StatsDto statsDto) {
        return Stats.builder()
                .app(statsDto.getApp())
                .uri(statsDto.getUri())
                .ip(statsDto.getIp())
                .timestamp(statsDto.getTimestamp())
                .build();
    }
}
