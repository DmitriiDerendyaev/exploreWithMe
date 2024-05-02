package org.example.category.service;

import org.example.category.dto.CategoryDto;
import org.example.category.dto.NewCategoryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CategoryService {

    public CategoryDto addCategoryAdmin(NewCategoryDto newCategoryDto);

    public void deleteCategoryAdmin(Long categoryId);

    public CategoryDto updateCategoryAdmin(CategoryDto categoryDto);

    public List<CategoryDto> getCategories(Pageable pageable);

    public CategoryDto getCategory(Long categoryId);
}
