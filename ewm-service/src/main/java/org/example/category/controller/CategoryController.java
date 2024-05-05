package org.example.category.controller;

import lombok.RequiredArgsConstructor;
import org.example.category.dto.CategoryDto;
import org.example.category.dto.NewCategoryDto;
import org.example.category.service.CategoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategoryAdmin(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return categoryService.addCategoryAdmin(newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryAdmin(@PathVariable Long id) {
        categoryService.deleteCategoryAdmin(id);
    }

    @PatchMapping("/admin/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategoryAdmin(@Valid @PathVariable Long id, @Valid @RequestBody CategoryDto categoryDto) {
        categoryDto.setId(id);
        return categoryService.updateCategoryAdmin(categoryDto);
    }


    @GetMapping("/categories")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
                                           @RequestParam(required = false, defaultValue = "10") @Min(1) Integer size) {
        return categoryService.getCategories(PageRequest.of(from / size, size));
    }

    @GetMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable Long id) {
        return categoryService.getCategory(id);
    }


}
