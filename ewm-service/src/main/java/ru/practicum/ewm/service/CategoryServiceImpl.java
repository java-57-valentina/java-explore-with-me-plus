package ru.practicum.ewm.service;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CategoryDtoOut;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;

import java.util.Collection;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Collection<CategoryDtoOut> getAll() {
        Collection<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDtoOut get(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category", id));

        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDtoOut add(CategoryDto categoryDto) {
        Category category = CategoryMapper.fromDto(categoryDto);
        try {
            Category saved = categoryRepository.save(category);
            return CategoryMapper.toDto(saved);
        }
        catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public CategoryDtoOut update(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));

        category.setName(categoryDto.getName());

        try {
            Category saved = categoryRepository.save(category);
            return CategoryMapper.toDto(saved);
        }
        catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
