package org.example.service;

import org.example.StatsDto;
import org.example.StatsDtoOutput;

import java.util.List;

public interface StatsService {

    public StatsDto saveStats(StatsDto statsDto);

    public List<StatsDtoOutput> getStats(String start,
                                         String end,
                                         List<String> uris,
                                         Boolean unique);
}
