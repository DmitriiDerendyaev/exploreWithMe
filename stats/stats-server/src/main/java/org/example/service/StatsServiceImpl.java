package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.StatsDto;
import org.example.StatsDtoOutput;
import org.example.exception.StatsValidationException;
import org.example.mapper.StatsMapper;
import org.example.repository.StatsRepository;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public StatsDto saveStats(StatsDto statsDto) {
        statsRepository.save(statsMapper.toStats(statsDto));
        log.info("Save new statistic info: {}", statsDto);
        return statsDto;
    }

    @Override
    public List<StatsDtoOutput> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Get statistic by parameter: Start({}), End({}), Uris({}), Unique({})",
                start, end, uris, unique);

        LocalDateTime startTime = parseTime(decode(start));
        LocalDateTime endTime = parseTime(decode(end));
        List<StatsDtoOutput> statsDtoOutputs;

        if (startTime.isAfter(endTime)) {
            throw new StatsValidationException("Start's time must be before end");
        }

        if (uris != null) {
            if (unique) {
                statsDtoOutputs = statsRepository.findAllStatsByTimeAndListOfUrisAndUniqueIp(startTime, endTime, uris);
            } else {
                statsDtoOutputs = statsRepository.findAllStatsByTimeAndListOfUris(startTime, endTime, uris);
            }
        } else if (unique) {
            statsDtoOutputs = statsRepository.findAllStatsByTimeAndUniqueIp(startTime, endTime);
        } else {
            statsDtoOutputs = statsRepository.findAllStatsByTime(startTime, endTime);
        }
        return statsDtoOutputs;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private LocalDateTime parseTime(String time) {
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            throw new StatsValidationException("Time's format must be: yyyy-MM-dd HH:mm:ss");
        }
    }

}


