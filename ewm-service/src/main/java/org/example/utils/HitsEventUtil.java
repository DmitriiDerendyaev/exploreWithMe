package org.example.utils;

import org.example.StatsClient;
import org.example.StatsDtoOutput;

import java.util.ArrayList;
import java.util.List;

public class HitsEventUtil {
    public static Long getHitsEvent(Long eventId, String start, String end, Boolean unique, StatsClient statsClient) {

        List<String> uris = new ArrayList<>();
        uris.add("/events/" + eventId);

        List<StatsDtoOutput> output = statsClient.getStats(start, end, uris, unique);

        Long view = 0L;

        if (!output.isEmpty()) {
            view = output.get(0).getHits();
        }
        return view;
    }
}
