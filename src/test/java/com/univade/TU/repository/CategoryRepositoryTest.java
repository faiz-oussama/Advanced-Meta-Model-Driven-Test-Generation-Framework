package com.univade.TU.repository;

import com.univade.TU.testdata.CategoryTestDataBuilder;


import com.univade.TU.entity.Category;


import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Category Repository Tests")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Category createCategory() {
        Category category = CategoryTestDataBuilder.aValidCategory().build();
        return category;
    }


    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createCategoryShouldPersistNewEntity() {
            Category testCategory = createCategory();
            Category createdCategory = categoryRepository.save(testCategory);

            assertThat(createdCategory).isNotNull();
            assertThat(createdCategory.getId()).isNotNull();
            assertThat(createdCategory.getId()).isPositive();

            Category found = entityManager.find(Category.class, createdCategory.getId());
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo(testCategory.getName());
            assertThat(found.getDescription()).isEqualTo(testCategory.getDescription());
            assertThat(found.getActive()).isEqualTo(testCategory.getActive());
        }

        @Test
        void createCategoryWithNullShouldThrowException() {
            assertThatThrownBy(() -> categoryRepository.save(null))
                    .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        void createMultipleCategorysShouldPersistAll() {
            Category category1 = createCategory();
            Category category2 = createCategory();
            Category category3 = createCategory();

            List<Category> createdCategorys = categoryRepository.saveAll(
                    List.of(category1, category2, category3));

            assertThat(createdCategorys).hasSize(3);
            assertThat(createdCategorys).allMatch(category -> category.getId() != null);

            long count = categoryRepository.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void readCategoryByIdShouldReturnCorrectEntity() {
            Category testCategory = createCategory();
            Category savedCategory = entityManager.persistAndFlush(testCategory);

            Optional<Category> found = categoryRepository.findById(savedCategory.getId());

            assertThat(found).isPresent();
            Category retrievedCategory = found.get();
            assertThat(retrievedCategory.getId()).isEqualTo(savedCategory.getId());
            assertThat(retrievedCategory.getName()).isEqualTo(testCategory.getName());
            assertThat(retrievedCategory.getDescription()).isEqualTo(testCategory.getDescription());
            assertThat(retrievedCategory.getActive()).isEqualTo(testCategory.getActive());
        }

        @Test
        void readCategoryByNonExistentIdShouldReturnEmpty() {
            Optional<Category> found = categoryRepository.findById(999L);

            assertThat(found).isEmpty();
        }

    @Test
    void readAllCategorysShouldReturnAllEntities() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());

        List<Category> allCategorys = categoryRepository.findAll();

            assertThat(allCategorys).hasSize(2);
            assertThat(allCategorys).extracting("id")
                    .contains(category1.getId(), category2.getId());
    }

    @Test
    void readAllCategorysWhenEmptyShouldReturnEmptyList() {
        List<Category> allCategorys = categoryRepository.findAll();

        assertThat(allCategorys).isEmpty();
    }

    @Test
    void updateCategoryShouldModifyExistingEntity() {
        Category testCategory = createCategory();
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        entityManager.detach(savedCategory);

        savedCategory.setName("Updated Name");

        Category updatedCategory = categoryRepository.save(savedCategory);

        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.getId()).isEqualTo(savedCategory.getId());
        assertThat(updatedCategory.getName()).isEqualTo("Updated Name");

        Category found = entityManager.find(Category.class, savedCategory.getId());
        assertThat(found.getName()).isEqualTo("Updated Name");
    }

    @Test
    void saveNewCategoryShouldCreateEntity() {
        long initialCount = categoryRepository.count();

        Category newCategory = createCategory();

        Category result = categoryRepository.save(newCategory);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        long finalCount = categoryRepository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void updateAllCategoryAttributesShouldPersistChanges() {
        Category testCategory = createCategory();
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        entityManager.detach(savedCategory);

        savedCategory.setName("Updated Name");

        Category updatedCategory = categoryRepository.save(savedCategory);

        assertThat(updatedCategory.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteCategoryByIdShouldRemoveEntity() {
        Category testCategory = createCategory();
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        categoryRepository.deleteById(savedCategory.getId());

        Optional<Category> found = categoryRepository.findById(savedCategory.getId());
        assertThat(found).isEmpty();

        Category entityFound = entityManager.find(Category.class, savedCategory.getId());
        assertThat(entityFound).isNull();
    }

        @Test
        void deleteCategoryByNonExistentIdShouldNotThrowException() {
            assertThatCode(() -> categoryRepository.deleteById(999L))
                    .doesNotThrowAnyException();
        }

    @Test
    void deleteCategoryEntityShouldRemoveFromDatabase() {
        Category testCategory = createCategory();
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        categoryRepository.delete(savedCategory);

        Optional<Category> found = categoryRepository.findById(savedCategory.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAllCategorysShouldRemoveAllEntities() {
        entityManager.persistAndFlush(createCategory());
        entityManager.persistAndFlush(createCategory());
        entityManager.persistAndFlush(createCategory());

        categoryRepository.deleteAll();

        List<Category> allCategorys = categoryRepository.findAll();
        assertThat(allCategorys).isEmpty();

        long count = categoryRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void deleteMultipleCategorysByIdShouldRemoveSpecifiedEntities() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());
        Category category3 = entityManager.persistAndFlush(createCategory());

        categoryRepository.deleteAllById(List.of(category1.getId(), category2.getId()));

        List<Category> remainingCategorys = categoryRepository.findAll();
        assertThat(remainingCategorys).hasSize(1);
        assertThat(remainingCategorys.get(0).getId()).isEqualTo(category3.getId());
    }

    @Test
    void existsCategoryByIdShouldReturnTrueForExistingEntity() {
        Category testCategory = createCategory();
        Category savedCategory = entityManager.persistAndFlush(testCategory);

        boolean exists = categoryRepository.existsById(savedCategory.getId());

        assertThat(exists).isTrue();
    }

        @Test
        void existsCategoryByIdShouldReturnFalseForNonExistingEntity() {
            boolean exists = categoryRepository.existsById(999L);

            assertThat(exists).isFalse();
        }

    @Test
    void countCategorysShouldReturnCorrectNumber() {
        entityManager.persistAndFlush(createCategory());
        entityManager.persistAndFlush(createCategory());

        long count = categoryRepository.count();

        assertThat(count).isEqualTo(2);
    }

        @Test
        void countCategorysWhenEmptyShouldReturnZero() {
            long count = categoryRepository.count();

            assertThat(count).isZero();
        }
    }



    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void findAllWithPaginationShouldReturnPagedResults() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());
        Category category3 = entityManager.persistAndFlush(createCategory());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Category> page = categoryRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAllWithPaginationSecondPageShouldReturnRemainingResults() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());
        Category category3 = entityManager.persistAndFlush(createCategory());

        Pageable pageable = PageRequest.of(1, 2);
        Page<Category> page = categoryRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void findAllWithSortingShouldReturnSortedResults() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());
        Category category3 = entityManager.persistAndFlush(createCategory());

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<Category> sortedCategorys = categoryRepository.findAll(sort);

        assertThat(sortedCategorys).hasSize(3);
        assertThat(sortedCategorys).isSortedAccordingTo((a, b) -> a.getName().compareTo(b.getName()));
    }

    @Test
    void findAllWithPaginationAndSortingShouldReturnPagedAndSortedResults() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());
        Category category3 = entityManager.persistAndFlush(createCategory());

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "name"));
        Page<Category> page = categoryRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).isSortedAccordingTo((a, b) -> b.getName().compareTo(a.getName()));
    }

    @Test
    void findAllWithEmptyPageShouldReturnEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = categoryRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void findAllWithLargePageSizeShouldReturnAllResults() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());

        Pageable pageable = PageRequest.of(0, 100);
        Page<Category> page = categoryRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void paginationWithMultipleSortFieldsShouldWork() {
        Category category1 = entityManager.persistAndFlush(createCategory());
        Category category2 = entityManager.persistAndFlush(createCategory());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Category> page = categoryRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
    }

    
}

}
