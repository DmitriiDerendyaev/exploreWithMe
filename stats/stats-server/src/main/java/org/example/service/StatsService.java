package org.example.service;

import org.example.StatsDto;
import org.example.StatsDtoOutput;

import java.util.List;

public interface StatsService {

    StatsDto saveStats(StatsDto statsDto);

    List<StatsDtoOutput> getStats(String start,
                                         String end,
                                         List<String> uris,
                                         Boolean unique);
}
