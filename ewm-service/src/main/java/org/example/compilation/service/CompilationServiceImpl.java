package org.example.compilation.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.StatsClient;
import org.example.StatsDtoOutput;
import org.example.compilation.dto.CompilationDto;
import org.example.compilation.dto.NewCompilationDto;
import org.example.compilation.dto.UpdateCompilationRequest;
import org.example.compilation.mapper.CompilationMapper;
import org.example.compilation.model.Compilation;
import org.example.compilation.repository.CompilationRepository;
import org.example.event.model.Event;
import org.example.event.repository.EventRepository;
import org.example.exceprtion.ObjectNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.constant.Constants.DATE_FORMAT;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final StatsClient client;

    @Override
    @Transactional
    public CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());

        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(newCompilationDto.getPinned());
        }

        compilationRepository.save(compilation);
        return compilationMapper.toDto(compilation, 0L);
    }

    @Override
    @Transactional
    public void deleteCompilationAdmin(Long compilationId) {
        if (compilationRepository.existsById(compilationId)) {
            compilationRepository.deleteById(compilationId);
            log.info(String.format("Compilation with id=%d not found", compilationId));
        }
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationAdmin(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilationOrThrow(compilationId);

        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findByIdIn(updateCompilationRequest.getEvents());
            compilation.setEvents(events);
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());

        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        compilationRepository.save(compilation);
        Long view = getHitsEvent(compilationId, LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), true);
        return compilationMapper.toDto(compilation, view);
    }

    @Override
    public List<CompilationDto> findCompilationsPublic(Boolean pinned, Pageable pageable) {
        if (pinned == null) {
            return compilationRepository.findAll(pageable).stream()
                    .map(c -> compilationMapper.toDto(c, getHitsEvent(c.getId(),
                            LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), true)))
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAllByPinned(pinned, pageable).stream()
                .map(c -> compilationMapper.toDto(c, getHitsEvent(c.getId(),
                        LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), true)))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationPublic(Long compilationId) {
        Compilation compilation = getCompilationOrThrow(compilationId);
        return compilationMapper.toDto(compilation, getHitsEvent(compilationId,
                LocalDateTime.now().minusDays(100).format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)), true));
    }

    private Compilation getCompilationOrThrow(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Compilation with id=%d was not found", compilationId)));
    }

    private Long getHitsEvent(Long eventId, String start, String end, Boolean unique) {

        List<String> uris = new ArrayList<>();
        uris.add("/events/" + eventId);

        List<StatsDtoOutput> output = client.getStats(start, end, uris, unique);

        Long view = 0L;

        if (!output.isEmpty()) {
            view = output.get(0).getHits();
        }
        return view;
    }
}