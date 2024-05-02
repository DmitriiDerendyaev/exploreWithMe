package org.example.compilation.service;

import org.example.compilation.dto.CompilationDto;
import org.example.compilation.dto.NewCompilationDto;
import org.example.compilation.dto.UpdateCompilationRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto);

    void deleteCompilationAdmin(long compilationId);

    CompilationDto updateCompilationAdmin(long compilationId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> findCompilationsPublic(Boolean pinned, Pageable pageable);

    CompilationDto findCompilationPublic(long compilationId);
}
