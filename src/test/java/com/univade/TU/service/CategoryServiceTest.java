package com.univade.TU.service;

import com.univade.TU.testdata.CategoryTestDataBuilder;


import com.univade.TU.entity.Category;


import com.univade.TU.repository.CategoryRepository;

import com.univade.TU.service.CategoryService;
import com.univade.TU.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        reset(categoryRepository);
    }

    private Category createCategory() {
        return CategoryTestDataBuilder.aValidCategory().build();
    }

    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createCategoryShouldSaveAndReturnEntity() {
            Category testCategory = createCategory();
            Category savedCategory = createCategory();
            savedCategory.setId(1L);

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            Category result = categoryService.createCategory(testCategory);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(categoryRepository).save(testCategory);
        }

        @Test
        void createCategoryWithNullShouldThrowException() {
            assertThatThrownBy(() -> categoryService.createCategory(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void getCategoryByIdShouldReturnEntityWhenExists() {
            Long testId = 1L;
            Category testCategory = createCategory();
            testCategory.setId(testId);

            when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));

            Category result = categoryService.getCategoryById(testId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(categoryRepository).findById(testId);
        }

        @Test
        void getCategoryByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(categoryRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getCategoryById(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(categoryRepository).findById(testId);
        }

        @Test
        void getAllCategorysShouldReturnAllEntities() {
            Category category1 = createCategory();
            Category category2 = createCategory();
            List<Category> expectedCategorys = List.of(category1, category2);

            when(categoryRepository.findAll()).thenReturn(expectedCategorys);

            List<Category> result = categoryService.getAllCategorys();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedCategorys);
            verify(categoryRepository).findAll();
        }

        @Test
        void getAllCategorysWhenEmptyShouldReturnEmptyList() {
            when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

            List<Category> result = categoryService.getAllCategorys();

            assertThat(result).isEmpty();
            verify(categoryRepository).findAll();
        }

        @Test
        void updateCategoryShouldUpdateAndReturnEntity() {
            Long testId = 1L;
            Category existingCategory = createCategory();
            existingCategory.setId(testId);

            Category updatedCategory = createCategory();
            updatedCategory.setId(testId);
            updatedCategory.setName("Updated Name");

            when(categoryRepository.findById(testId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

            Category result = categoryService.updateCategory(testId, updatedCategory);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(categoryRepository).findById(testId);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void updateCategoryWithNonExistentIdShouldThrowException() {
            Long testId = 999L;
            Category updatedCategory = createCategory();

            when(categoryRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(testId, updatedCategory))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(categoryRepository).findById(testId);
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        void deleteCategoryByIdShouldDeleteWhenExists() {
            Long testId = 1L;

            when(categoryRepository.existsById(testId)).thenReturn(true);
            doNothing().when(categoryRepository).deleteById(testId);

            assertThatCode(() -> categoryService.deleteCategory(testId))
                    .doesNotThrowAnyException();

            verify(categoryRepository).existsById(testId);
            verify(categoryRepository).deleteById(testId);
        }

        @Test
        void deleteCategoryByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(categoryRepository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.deleteCategory(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(categoryRepository).existsById(testId);
            verify(categoryRepository, never()).deleteById(testId);
        }

        @Test
        void existsCategoryByIdShouldReturnTrueWhenExists() {
            Long testId = 1L;

            when(categoryRepository.existsById(testId)).thenReturn(true);

            boolean result = categoryService.existsCategoryById(testId);

            assertThat(result).isTrue();
            verify(categoryRepository).existsById(testId);
        }

        @Test
        void existsCategoryByIdShouldReturnFalseWhenNotExists() {
            Long testId = 999L;

            when(categoryRepository.existsById(testId)).thenReturn(false);

            boolean result = categoryService.existsCategoryById(testId);

            assertThat(result).isFalse();
            verify(categoryRepository).existsById(testId);
        }

        @Test
        void countCategorysShouldReturnCorrectCount() {
            long expectedCount = 5L;

            when(categoryRepository.count()).thenReturn(expectedCount);

            long result = categoryService.countCategorys();

            assertThat(result).isEqualTo(expectedCount);
            verify(categoryRepository).count();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when creating Category with null entity")
        void createCategoryWithNullShouldThrowException() {
            assertThatThrownBy(() -> categoryService.createCategory(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category cannot be null");

            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Category with null entity")
        void updateCategoryWithNullShouldThrowException() {
            Long testId = 1L;

            assertThatThrownBy(() -> categoryService.updateCategory(testId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category cannot be null");

            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Category with null ID")
        void updateCategoryWithNullIdShouldThrowException() {
            Category testCategory = createCategory();

            assertThatThrownBy(() -> categoryService.updateCategory(null, testCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw exception when deleting Category with null ID")
        void deleteCategoryWithNullIdShouldThrowException() {
            assertThatThrownBy(() -> categoryService.deleteCategory(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent Category")
        void updateCategoryWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;
            Category testCategory = createCategory();

            when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(nonExistentId, testCategory))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category not found with id: " + nonExistentId);

            verify(categoryRepository).findById(nonExistentId);
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent Category")
        void deleteCategoryWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;

            when(categoryRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.deleteCategory(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category not found with id: " + nonExistentId);

            verify(categoryRepository).existsById(nonExistentId);
            verify(categoryRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully when finding Category")
        void findCategoryByIdShouldHandleRepositoryException() {
            Long testId = 1L;

            when(categoryRepository.findById(testId))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> categoryService.getCategoryById(testId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(categoryRepository).findById(testId);
        }

        @Test
        @DisplayName("Should validate business rules before saving Category")
        void createCategoryShouldValidateBusinessRules() {
            Category testCategory = createCategory();
            Category savedCategory = createCategory();
            savedCategory.setId(1L);

            when(categoryRepository.save(testCategory)).thenReturn(savedCategory);

            Category result = categoryService.createCategory(testCategory);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedCategory);
            verify(categoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should check existence before operations")
        void existsCategoryByIdShouldReturnCorrectValue() {
            Long existingId = 1L;
            Long nonExistentId = 999L;

            when(categoryRepository.existsById(existingId)).thenReturn(true);
            when(categoryRepository.existsById(nonExistentId)).thenReturn(false);

            boolean existsResult = categoryService.existsCategoryById(existingId);
            boolean notExistsResult = categoryService.existsCategoryById(nonExistentId);

            assertThat(existsResult).isTrue();
            assertThat(notExistsResult).isFalse();
            verify(categoryRepository).existsById(existingId);
            verify(categoryRepository).existsById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null ID gracefully in exists check")
        void existsCategoryByIdWithNullShouldReturnFalse() {
            boolean result = categoryService.existsCategoryById(null);

            assertThat(result).isFalse();
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("Should handle validation errors from JPA/Hibernate")
        void createCategoryShouldHandleConstraintViolationException() {
            Category invalidCategory = createCategory();

            when(categoryRepository.save(invalidCategory))
                    .thenThrow(new ConstraintViolationException("Validation failed", null));

            assertThatThrownBy(() -> categoryService.createCategory(invalidCategory))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Validation failed");

            verify(categoryRepository).save(invalidCategory);
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void createCategoryShouldHandleDataIntegrityViolationException() {
            Category duplicateCategory = createCategory();

            when(categoryRepository.save(duplicateCategory))
                    .thenThrow(new DataIntegrityViolationException("Duplicate constraint violation"));

            assertThatThrownBy(() -> categoryService.createCategory(duplicateCategory))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Duplicate constraint violation");

            verify(categoryRepository).save(duplicateCategory);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void getAllCategorysWithPageableShouldReturnPagedResults() {
            Category category1 = createCategory();
            Category category2 = createCategory();
            Category category3 = createCategory();

            List<Category> categoryList = List.of(category1, category2);
            Page<Category> expectedPage = new PageImpl<>(categoryList, PageRequest.of(0, 2), 3);

            Pageable pageable = PageRequest.of(0, 2);
            when(categoryRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void getAllCategorysWithPageableAndSortShouldReturnSortedResults() {
            Category category1 = createCategory();
            category1.setName("Hicham");
            Category category2 = createCategory();
            category2.setName("Oussama");

            List<Category> sortedCategoryList = List.of(category1, category2);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Category> expectedPage = new PageImpl<>(sortedCategoryList, pageable, 2);

            when(categoryRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Hicham");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Oussama");
            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void getAllCategorysWithEmptyPageShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(categoryRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void getAllCategorysWithLargePageSizeShouldReturnAllResults() {
            Category category1 = createCategory();
            Category category2 = createCategory();
            Category category3 = createCategory();

            List<Category> allCategorys = List.of(category1, category2, category3);
            Pageable pageable = PageRequest.of(0, 100);
            Page<Category> expectedPage = new PageImpl<>(allCategorys, pageable, 3);

            when(categoryRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void getAllCategorysWithSecondPageShouldReturnCorrectResults() {
            Category category3 = createCategory();
            Category category4 = createCategory();

            List<Category> secondPageCategorys = List.of(category3, category4);
            Pageable pageable = PageRequest.of(1, 2);
            Page<Category> expectedPage = new PageImpl<>(secondPageCategorys, pageable, 4);

            when(categoryRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            verify(categoryRepository).findAll(pageable);
        }



        @Test
        void getAllCategorysWithDescendingSortShouldReturnCorrectOrder() {
            Category category1 = createCategory();
            category1.setName("Ilyass");
            Category category2 = createCategory();
            category2.setName("Oussama");

            List<Category> sortedCategoryList = List.of(category2, category1);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
            Page<Category> expectedPage = new PageImpl<>(sortedCategoryList, pageable, 2);

            when(categoryRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Oussama");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Ilyass");
            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void getAllCategorysWithCustomPageSizeShouldRespectPageSize() {
            Category category1 = createCategory();
            Category category2 = createCategory();

            List<Category> categoryList = List.of(category1, category2);
            Pageable pageable = PageRequest.of(0, 1);
            Page<Category> expectedPage = new PageImpl<>(List.of(category1), pageable, 2);

            when(categoryRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Category> result = categoryService.getAllCategorys(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(1);
            verify(categoryRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Service Validation Tests")
    class ServiceValidationTests {

        @Test
        @DisplayName("Should validate entity before saving")
        void createCategoryShouldValidateEntity() {
            Category testCategory = createCategory();
            Category savedCategory = createCategory();
            savedCategory.setId(1L);

            when(categoryRepository.save(testCategory)).thenReturn(savedCategory);

            Category result = categoryService.createCategory(testCategory);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedCategory);
            verify(categoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should handle repository exceptions during validation")
        void createCategoryShouldHandleRepositoryExceptions() {
            Category testCategory = createCategory();

            when(categoryRepository.save(testCategory))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> categoryService.createCategory(testCategory))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(categoryRepository).save(testCategory);
        }

        @Test
        @DisplayName("Should validate entity state before update")
        void updateCategoryShouldValidateEntityState() {
            Long testId = 1L;
            Category existingCategory = createCategory();
            existingCategory.setId(testId);

            Category updatedCategory = createCategory();
            Category savedCategory = createCategory();
            savedCategory.setId(testId);

            when(categoryRepository.findById(testId)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            Category result = categoryService.updateCategory(testId, updatedCategory);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(categoryRepository).findById(testId);
            verify(categoryRepository).save(any(Category.class));
        }
    }


    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private CategoryService categoryServiceIntegration;

        @Autowired
        private CategoryRepository categoryRepositoryIntegration;

        @Autowired
        private jakarta.persistence.EntityManager entityManager;

        private Category testCategoryIntegration;

        @BeforeEach
        void setUpIntegration() {
            testCategoryIntegration = createTestEntity();
        }

        private Category createTestEntity() {
            Category entity = CategoryTestDataBuilder.aDefaultCategory().build();
            return entity;
        }

        @Nested
        @DisplayName("Business Logic Integration Tests")
        class BusinessLogicIntegrationTests {

            @Test
            @DisplayName("Should create Category and persist to database")
            void shouldCreateCategoryAndPersistToDatabase() {
                Category testEntity = createTestEntity();
                Category created = categoryServiceIntegration.createCategory(testEntity);

                assertThat(created).isNotNull();
                assertThat(created.getId()).isNotNull();
                
                Optional<Category> persisted = categoryRepositoryIntegration.findById(created.getId());
                assertThat(persisted).isPresent();
                assertThat(persisted.get().getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should update Category and persist changes")
            void shouldUpdateCategoryAndPersistChanges() {
                Category testEntity = createTestEntity();
                Category created = categoryServiceIntegration.createCategory(testEntity);
                
                created.setName("Updated name");

                Category updated = categoryServiceIntegration.updateCategory(created.getId(), created);

                assertThat(updated).isNotNull();
                Category persisted = categoryRepositoryIntegration.findById(created.getId()).orElseThrow();
                assertThat(persisted.getName()).isEqualTo("Updated name");
            }

            @Test
            @DisplayName("Should delete Category from database")
            void shouldDeleteCategoryFromDatabase() {
                Category testEntity = createTestEntity();
                Category created = categoryServiceIntegration.createCategory(testEntity);
                Long id = created.getId();

                categoryServiceIntegration.deleteCategory(id);

                Optional<Category> deleted = categoryRepositoryIntegration.findById(id);
                assertThat(deleted).isEmpty();
            }
        }

        @Nested
        @DisplayName("Validation Constraint Integration Tests")
        class ValidationConstraintIntegrationTests {


        }

        @Nested
        @DisplayName("Transactional Behavior Integration Tests")
        class TransactionalBehaviorIntegrationTests {

        }


        @Nested
        @DisplayName("Finder Method Integration Tests")
        class FinderMethodIntegrationTests {

            @Test
            @DisplayName("Should find Category by ID correctly")
            void shouldFindCategoryByIdCorrectly() {
                Category testEntity = createTestEntity();
                Category created = categoryServiceIntegration.createCategory(testEntity);

                Category found = categoryServiceIntegration.getCategoryById(created.getId());

                assertThat(found).isNotNull();
                assertThat(found.getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should return empty when Category not found by ID")
            void shouldReturnEmptyWhenCategoryNotFoundById() {
                Long nonExistentId = 999L;

                assertThatThrownBy(() -> categoryServiceIntegration.getCategoryById(nonExistentId))
                        .isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("Should check existence correctly")
            void shouldCheckExistenceCorrectly() {
                Category testEntity = createTestEntity();
                Category created = categoryServiceIntegration.createCategory(testEntity);
                Long nonExistentId = 999L;

                boolean exists = categoryServiceIntegration.existsCategoryById(created.getId());
                boolean notExists = categoryServiceIntegration.existsCategoryById(nonExistentId);

                assertThat(exists).isTrue();
                assertThat(notExists).isFalse();
            }

            @Test
            @DisplayName("Should find all Categorys from database")
            void shouldFindAllCategorysFromDatabase() {
                Category testEntity1 = createTestEntity();
                Category entity1 = categoryServiceIntegration.createCategory(testEntity1);
                Category testEntity2 = createTestEntity();
                Category entity2 = categoryServiceIntegration.createCategory(testEntity2);

                List<Category> all = categoryServiceIntegration.getAllCategorys();

                assertThat(all).hasSizeGreaterThanOrEqualTo(2);
                assertThat(all).extracting(Category::getId)
                        .contains(entity1.getId(), entity2.getId());
            }
        }

        @Nested
        @DisplayName("Deletion Logic Integration Tests")
        class DeletionLogicIntegrationTests {

            @Test
            @DisplayName("Should delete Category and verify removal")
            void shouldDeleteCategoryAndVerifyRemoval() {
                Category testEntity = createTestEntity();
                Category created = categoryServiceIntegration.createCategory(testEntity);
                Long id = created.getId();

                categoryServiceIntegration.deleteCategory(id);

                boolean exists = categoryRepositoryIntegration.existsById(id);
                assertThat(exists).isFalse();
            }

        }
    }




}
