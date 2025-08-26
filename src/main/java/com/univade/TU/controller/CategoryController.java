package com.univade.TU.controller;

import com.univade.TU.dto.CategoryDto;
import com.univade.TU.entity.Category;
import com.univade.TU.exception.BadRequestException;
import com.univade.TU.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        if (categoryDto == null) {
            throw new BadRequestException("Request body cannot be null");
        }
        Category category = convertToEntity(categoryDto);
        Category createdCategory = categoryService.createCategory(category);
        CategoryDto responseDto = convertToDto(createdCategory);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCategory.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        if (id <= 0) {
            throw new BadRequestException("ID must be positive");
        }
        Category category = categoryService.getCategoryById(id);
        CategoryDto categoryDto = convertToDto(category);
        return ResponseEntity.ok(categoryDto);
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategorys();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<CategoryDto>> getAllCategories(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException("Page number must be non-negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("Page size must be positive");
        }
        if (pageable.getPageSize() > 1000) {
            throw new BadRequestException("Page size cannot exceed 1000");
        }

        Page<Category> categories = categoryService.getAllCategorys(pageable);
        Page<CategoryDto> categoryDtos = categories.map(this::convertToDto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(categoryDtos.getTotalElements()))
                .body(categoryDtos);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto categoryDto) {
        Category category = convertToEntity(categoryDto);
        Category updatedCategory = categoryService.updateCategory(id, category);
        CategoryDto responseDto = convertToDto(updatedCategory);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsCategoryById(@PathVariable Long id) {
        boolean exists = categoryService.existsCategoryById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countCategories() {
        long count = categoryService.countCategorys();
        return ResponseEntity.ok(count);
    }

    private CategoryDto convertToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .build();
    }

    private Category convertToEntity(CategoryDto categoryDto) {
        Category category = new Category();
        category.setId(categoryDto.getId());
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setActive(categoryDto.getActive());
        return category;
    }
}
