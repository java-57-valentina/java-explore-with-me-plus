package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CategoryDtoOut;
import ru.practicum.ewm.service.CategoryService;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @GetMapping("/categories")
    public Collection<CategoryDtoOut> getCategories() {
        return categoryService.getAll();
    }

    @GetMapping("/categories/{id}")
    public CategoryDtoOut getCategory(@PathVariable @Min(1) Long id) {
        return categoryService.get(id);
    }



    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDtoOut createCategory(@Validated @RequestBody CategoryDto categoryDto) {
        log.info("Create category '{}' by admin", categoryDto.getName());
        return categoryService.add(categoryDto);
    }

    @PatchMapping("/admin/categories/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDtoOut updateCategory(@Validated @RequestBody CategoryDto categoryDto,
                                         @PathVariable Long id) {
        log.info("Update category id:{} by admin", id);
        return categoryService.update(id, categoryDto);
    }

    @DeleteMapping("/admin/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("delete category id:{} by admin", id);
        categoryService.delete(id);
    }
}
