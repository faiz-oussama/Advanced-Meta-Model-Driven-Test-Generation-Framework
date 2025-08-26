package com.univade.TU.service;

import com.univade.TU.entity.Category;
import com.univade.TU.exception.EntityNotFoundException;
import com.univade.TU.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategorys() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Category> getAllCategorys(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Category updateCategory(Long id, Category category) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setActive(category.getActive());
        
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsCategoryById(Long id) {
        if (id == null) {
            return false;
        }
        return categoryRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long countCategorys() {
        return categoryRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<Category> findCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Category> findActiveCategorys() {
        return categoryRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public List<Category> findInactiveCategorys() {
        return categoryRepository.findByActive(false);
    }

    @Transactional(readOnly = true)
    public List<Category> findCategoriesByNameContaining(String nameFragment) {
        return categoryRepository.findByNameContaining(nameFragment);
    }
}
