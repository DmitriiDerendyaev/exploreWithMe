package org.example.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.example.compilation.dto.CompilationDto;
import org.example.compilation.dto.NewCompilationDto;
import org.example.compilation.dto.UpdateCompilationRequest;
import org.example.compilation.service.CompilationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CompilationController {
    private final CompilationService compilationService;

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilationAdmin(@Valid @RequestBody NewCompilationDto newCompilationDto) {

        return compilationService.addCompilationAdmin(newCompilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationAdmin(@PathVariable Long compId) {
        compilationService.deleteCompilationAdmin(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilationAdmin(@PathVariable Long compId,
                                                 @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        return compilationService.updateCompilationAdmin(compId, updateCompilationRequest);
    }

//    @GetMapping("/compilations")
//    @ResponseStatus(HttpStatus.OK)
//    public List<CompilationDto> findCompilationsPublicDemo(@RequestParam(required = false, defaultValue = "false") String pinned,
//                                                       @RequestParam(required = false, defaultValue = "0") Integer from,
//                                                       @RequestParam(required = false, defaultValue = "10") Integer size) {
//        return compilationService.findCompilationsPublic(Boolean.valueOf(pinned), PageRequest.of(from / size, size));
//    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> findCompilationPublic(@RequestParam(defaultValue = "false") String pinned,
                                                      @RequestParam(defaultValue = "0") Integer from,
                                                      @RequestParam(defaultValue = "10") Integer size) {
        return compilationService.findCompilationsPublic(Boolean.valueOf(pinned), PageRequest.of(from / size, size));
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto findCompilationPublic(@PathVariable Long compId) {
        return compilationService.findCompilationPublic(compId);
    }
}