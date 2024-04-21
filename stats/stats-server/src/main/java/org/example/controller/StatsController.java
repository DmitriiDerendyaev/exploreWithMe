package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.StatsDto;
import org.example.service.StatsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    public StatsDto saveStats(@Valid @RequestBody StatsDto statsDto) {
        return statsService.saveStats(statsDto);
    }


}
