package org.example.category.service;

import org.example.category.dto.CategoryDto;
import org.example.category.dto.NewCategoryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CategoryService {

    CategoryDto addCategoryAdmin(NewCategoryDto newCategoryDto);

    void deleteCategoryAdmin(Long categoryId);

    CategoryDto updateCategoryAdmin(CategoryDto categoryDto);

    List<CategoryDto> getCategories(Pageable pageable);

    CategoryDto getCategory(Long categoryId);
}
