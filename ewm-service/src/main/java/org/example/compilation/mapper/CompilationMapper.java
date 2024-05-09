package org.example.compilation.mapper;


import lombok.RequiredArgsConstructor;
import org.example.compilation.dto.CompilationDto;
import org.example.compilation.model.Compilation;
import org.example.event.mapper.EventMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public CompilationDto toDto(Compilation compilation, Long view) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.getPinned() != null && compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setEvents(compilation.getEvents().stream()
                .map(x -> eventMapper.toShort(x, view))
                .collect(Collectors.toList()));
        return compilationDto;
    }
}
