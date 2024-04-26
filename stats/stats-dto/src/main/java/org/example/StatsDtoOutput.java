package org.example;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class StatsDtoOutput {
    private String app;
    private String uri;
    private Long hits;

    public StatsDtoOutput(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}