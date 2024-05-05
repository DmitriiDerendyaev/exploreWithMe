package org.example.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.category.dto.CategoryDto;
import org.example.category.dto.NewCategoryDto;
import org.example.category.mapper.CategoryMapper;
import org.example.category.model.Category;
import org.example.category.repository.CategoryRepository;
import org.example.event.repository.EventRepository;
import org.example.exceprtion.ObjectAlreadyExistException;
import org.example.exceprtion.ObjectNotFoundException;
import org.example.exceprtion.RulesViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategoryAdmin(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ObjectAlreadyExistException("Category with name: {} already exist");
        }
        log.info("Add new category: {}", newCategoryDto.toString());
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(Long categoryId) {
        getCategoryOrThrow(categoryId);
        if (eventRepository.findAllByCategoryId(categoryId) != null) {
            throw new RulesViolationException(
                    String.format("Category with id=%d can't remove, because have events", categoryId));
        }
        categoryRepository.deleteById(categoryId);
        log.info("Category was deleted");
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryAdmin(CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(categoryDto.getId()).orElseThrow(
                () -> new ObjectNotFoundException(
                        String.format("Category with ID:%d was not found", categoryDto.getId())));

        // Проверяем, изменяется ли имя категории на уже существующее
        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByName(categoryDto.getName())) {
            throw new ObjectAlreadyExistException("Category with this name already exists");
        }

        existingCategory.setName(categoryDto.getName());
        log.info("Update category: {}", categoryDto.toString());
        return categoryMapper.toCategoryDto(categoryRepository.save(existingCategory));

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

    private Category getCategoryOrThrow(long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException(
                        String.format("Category with id=%d was not found", id)));
    }
}
