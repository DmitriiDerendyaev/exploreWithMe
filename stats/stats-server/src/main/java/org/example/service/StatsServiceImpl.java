package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.StatsDto;
import org.example.mapper.StatsMapper;
import org.example.repository.StatsRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    public StatsDto saveStats(StatsDto statsDto) {
        statsRepository.save(statsMapper.toStats(statsDto));
        log.info("Save new statistic info: {}", statsDto);
        return statsDto;
    }

}


