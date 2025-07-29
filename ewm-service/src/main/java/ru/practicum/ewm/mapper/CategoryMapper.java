package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CategoryDtoOut;
import ru.practicum.ewm.model.Category;

@UtilityClass
public class CategoryMapper {

    public static CategoryDtoOut toDto(Category category) {
        return new CategoryDtoOut(category.getId(), category.getName());
    }

    public static Category fromDto(CategoryDto dto) {
        return new Category(null, dto.getName());
    }
}
