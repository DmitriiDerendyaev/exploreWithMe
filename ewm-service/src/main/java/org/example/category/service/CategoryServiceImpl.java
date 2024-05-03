package org.example.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.category.dto.CategoryDto;
import org.example.category.dto.NewCategoryDto;
import org.example.category.mapper.CategoryMapper;
import org.example.category.model.Category;
import org.example.category.repository.CategoryRepository;
import org.example.exceprtion.ObjectAlreadyExistException;
import org.example.exceprtion.ObjectNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategoryAdmin(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ObjectAlreadyExistException("Category with name: {} already exist");
        }
        log.info("Add new category: {}", newCategoryDto.toString());
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    public void deleteCategoryAdmin(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ObjectNotFoundException(String.format("Category with ID: %d does not exist", categoryId));
        }
        // TODO: добавить проверку на удаление с events
        log.info("Category with ID: {} was deleted", categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public CategoryDto updateCategoryAdmin(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.getId()).orElseThrow(
                () -> new ObjectNotFoundException(
                        String.format("Category with ID:%d was not found", categoryDto.getId())));
        category.setName(categoryDto.getName());
        log.info("Update category: {}", categoryDto.toString());
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        List<CategoryDto> allCategories = categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
        log.info("Find all categories with parameters by PUBLIC");
        return allCategories;
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ObjectNotFoundException(
                        String.format("Category with ID:%d was not found", categoryId)));
        log.info("Get category: {} by PUBLIC", category.toString());
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}
