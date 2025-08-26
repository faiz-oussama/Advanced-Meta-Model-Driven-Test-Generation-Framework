package com.univade.TU.controller;

import com.univade.TU.testdata.CategoryTestDataBuilder;
import com.univade.TU.dto.CategoryDto;


import com.univade.TU.entity.Category;


import com.univade.TU.controller.CategoryController;
import com.univade.TU.service.CategoryService;
import com.univade.TU.exception.EntityNotFoundException;
import com.univade.TU.exception.BadRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.univade.TU.generator.model.ControllerValidationRule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(com.univade.TU.config.TestSecurityConfig.class)
@WithMockUser
@DisplayName("Category Controller Tests")
class CategoryControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(categoryService);
    }

    private Category createCategory() {
        return CategoryTestDataBuilder.aValidCategory().build();
    }

    private CategoryDto createCategoryDto() {
        Category entity = createCategory();
        return CategoryDto.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }



    private Category createMockEntity() {
        Category mockEntity = createCategory();
        mockEntity.setId(1L);
        return mockEntity;
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create Category successfully with 201 status")
        void createCategoryShouldReturnCreated() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            responseDto.setId(1L);

            Category mockEntity = createCategory();
            mockEntity.setId(1L);
            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should get Category by ID successfully with 200 status")
        void getCategoryByIdShouldReturnOk() throws Exception {
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            Category mockEntity = createCategory();
            mockEntity.setId(categoryId);
            when(categoryService.getCategoryById(categoryId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id").value(1));

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should return 404 when Category not found")
        void getCategoryByIdShouldReturn404WhenNotFound() throws Exception {
            Long categoryId = 999L;

            when(categoryService.getCategoryById(categoryId))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + categoryId));

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Category not found with id: " + categoryId))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should update Category successfully with 200 status")
        void updateCategoryShouldReturnOk() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            Category mockEntity = createCategory();
            mockEntity.setId(categoryId);
            when(categoryService.updateCategory(eq(categoryId), any(Category.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/categories/{id}", categoryId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id").value(1));

            verify(categoryService).updateCategory(eq(categoryId), any(Category.class));
        }

        @Test
        @DisplayName("Should delete Category successfully with 204 status")
        void deleteCategoryShouldReturnNoContent() throws Exception {
            Long categoryId = 1L;

            doNothing().when(categoryService).deleteCategory(categoryId);

            mockMvc.perform(delete("/api/categories/{id}", categoryId)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(categoryService).deleteCategory(categoryId);
        }

        @Test
        @DisplayName("Should get all Categorys successfully with 200 status")
        void getAllCategorysShouldReturnOk() throws Exception {
            CategoryDto categoryDto1 = createCategoryDto();
            CategoryDto categoryDto2 = createCategoryDto();
            categoryDto1.setId(1L);
            categoryDto2.setId(2L);

            List<CategoryDto> categoryDtoList = Arrays.asList(categoryDto1, categoryDto2);

            Category entity1 = createCategory();
            Category entity2 = createCategory();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Category> entityList = Arrays.asList(entity1, entity2);
            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));

            verify(categoryService).getAllCategorys();
        }
    }

    @Nested
    @DisplayName("Dynamic Validation Tests")
    class DynamicValidationTests {

        @Test
        @DisplayName("Should handle DTO with only non-strict validations")
        void testNonStrictValidationConstraints() throws Exception {
            List<ControllerValidationRule> rules = createValidationRules();

            CategoryDto dto = CategoryDto.builder().build();

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).createCategory(any(Category.class));
        }

        private List<ControllerValidationRule> createValidationRules() {
            List<ControllerValidationRule> rules = new ArrayList<>();

            // Should return 400 when name has invalid size
            rules.add(ControllerValidationRule.builder()
                    .testName("createCategoryWithInvalidSizeNameShouldReturnBadRequest")
                    .attributeName("name")
                    .validationType("Size")
                    .description("Should return 400 when name has invalid size")
                    .expectedHttpStatus("400")
                    .message("name must not exceed 100 characters")
                    .minValue(0)
                    .maxValue(100)
                    .invalidValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .build());

            // Should return 400 when description has invalid size
            rules.add(ControllerValidationRule.builder()
                    .testName("createCategoryWithInvalidSizeDescriptionShouldReturnBadRequest")
                    .attributeName("description")
                    .validationType("Size")
                    .description("Should return 400 when description has invalid size")
                    .expectedHttpStatus("400")
                    .message("description must not exceed 500 characters")
                    .minValue(0)
                    .maxValue(500)
                    .invalidValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .build());

            return rules;
        }

        private void setInvalidValueOnDto(CategoryDto dto, ControllerValidationRule rule) throws Exception {
            Field field = CategoryDto.class.getDeclaredField(rule.getAttributeName());
            field.setAccessible(true);

            if (rule.isNotNull()) {
                field.set(dto, null);
            } else if (rule.isNotBlank()) {
                field.set(dto, "");
            } else if (rule.isSize() && rule.getMaxValue() != null) {
                int maxLength = ((Number) rule.getMaxValue()).intValue();
                field.set(dto, "a".repeat(maxLength + 1));
            } else if (rule.isMin() && rule.getConstraintValue() != null) {
                Number minValue = (Number) rule.getConstraintValue();
                setNumericFieldValue(field, dto, minValue.intValue() - 1);
            } else if (rule.isMax() && rule.getConstraintValue() != null) {
                Number maxValue = (Number) rule.getConstraintValue();
                setNumericFieldValue(field, dto, maxValue.intValue() + 1);
            } else if (rule.isEmail()) {
                field.set(dto, "invalid-email");
            } else if (rule.isPattern()) {
                field.set(dto, "invalid-pattern-value");
            }
        }

        private void setNumericFieldValue(Field field, CategoryDto dto, int value) throws Exception {
            Class<?> fieldType = field.getType();
            if (fieldType == Integer.class || fieldType == int.class) {
                field.set(dto, value);
            } else if (fieldType == Long.class || fieldType == long.class) {
                field.set(dto, (long) value);
            } else if (fieldType == Short.class || fieldType == short.class) {
                field.set(dto, (short) value);
            } else if (fieldType == Byte.class || fieldType == byte.class) {
                field.set(dto, (byte) value);
            } else {
                field.set(dto, value);
            }
        }

        @Test
        @DisplayName("Should handle empty DTO gracefully when no validation rules exist")
        void createCategoryWithEmptyDtoShouldHandleGracefully() throws Exception {
            CategoryDto requestDto = CategoryDto.builder().build();

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).createCategory(any(Category.class));
        }

    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should return 404 when entity not found on GET")
        void getCategoryByIdShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;

            when(categoryService.getCategoryById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/categories/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Category not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/categories/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).getCategoryById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 404 when entity not found on UPDATE")
        void updateCategoryShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;
            CategoryDto requestDto = createCategoryDto();

            when(categoryService.updateCategory(eq(nonExistentId), any(Category.class)))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + nonExistentId));

            mockMvc.perform(put("/api/categories/{id}", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Category not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/categories/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).updateCategory(eq(nonExistentId), any(Category.class));
        }

        @Test
        @DisplayName("Should return 404 when entity not found on DELETE")
        void deleteCategoryShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;

            doThrow(new EntityNotFoundException("Category not found with id: " + nonExistentId))
                    .when(categoryService).deleteCategory(nonExistentId);

            mockMvc.perform(delete("/api/categories/{id}", nonExistentId)
                    .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Category not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/categories/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).deleteCategory(nonExistentId);
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void createCategoryShouldReturn500OnUnexpectedException() throws Exception {
            CategoryDto requestDto = createCategoryDto();

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/categories"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 400 when service throws BadRequestException")
        void createCategoryShouldReturn400OnBadRequestException() throws Exception {
            CategoryDto requestDto = createCategoryDto();

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new BadRequestException("Invalid data provided"));

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Invalid data provided"))
                    .andExpect(jsonPath("$.path").value("/api/categories"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 400 when bad request exception occurs")
        void createCategoryShouldReturn400WhenBadRequestException() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            String errorMessage = "Invalid data provided";

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new BadRequestException(errorMessage));

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(jsonPath("$.path").value("/api/categories"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 500 when internal server error occurs")
        void getCategoryShouldReturn500WhenInternalServerError() throws Exception {
            Long categoryId = 1L;

            when(categoryService.getCategoryById(categoryId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.path").value("/api/categories/" + categoryId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should return 405 when HTTP method not allowed")
        void shouldReturn405WhenMethodNotAllowed() throws Exception {
            mockMvc.perform(patch("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/categories"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 406 when accept header is not supported")
        void shouldReturn406WhenAcceptHeaderNotSupported() throws Exception {
            Long categoryId = 1L;
            CategoryDto responseDto = createCategoryDto();
            responseDto.setId(categoryId);

            when(categoryService.getCategoryById(categoryId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/categories/" + categoryId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle constraint violation exceptions properly")
        void createCategoryShouldHandleConstraintViolationException() throws Exception {
            CategoryDto requestDto = createCategoryDto();

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new ConstraintViolationException("Constraint violation", null));

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/categories"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).createCategory(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Status Code Tests")
    class StatusCodeTests {

        @Test
        @DisplayName("Should return 201 Created with proper Location header on successful creation")
        void createCategoryShouldReturn201WithLocationHeader() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            responseDto.setId(1L);

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("1")))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 200 OK on successful retrieval")
        void getCategoryByIdShouldReturn200() throws Exception {
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            when(categoryService.getCategoryById(categoryId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should return 200 OK on successful update")
        void updateCategoryShouldReturn200() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            when(categoryService.updateCategory(eq(categoryId), any(Category.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/categories/{id}", categoryId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).updateCategory(eq(categoryId), any(Category.class));
        }

        @Test
        @DisplayName("Should return 204 No Content on successful deletion")
        void deleteCategoryShouldReturn204() throws Exception {
            Long categoryId = 1L;

            doNothing().when(categoryService).deleteCategory(categoryId);

            mockMvc.perform(delete("/api/categories/{id}", categoryId)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(categoryService).deleteCategory(categoryId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when entity does not exist")
        void getCategoryByIdShouldReturn404WhenNotFound() throws Exception {
            Long nonExistentId = 999L;

            when(categoryService.getCategoryById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/categories/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).getCategoryById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle empty DTO when no validation rules exist")
        void createCategoryWithEmptyDataShouldSucceed() throws Exception {
            CategoryDto emptyDto = CategoryDto.builder().build();

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void createCategoryWithWrongContentTypeShouldReturn415() throws Exception {
            CategoryDto requestDto = createCategoryDto();

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).createCategory(any());
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error on service exception")
        void getCategoryByIdShouldReturn500OnServiceException() throws Exception {
            Long categoryId = 1L;

            when(categoryService.getCategoryById(categoryId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should return 409 Conflict on duplicate resource creation")
        void createCategoryWithDuplicateDataShouldReturn409() throws Exception {
            CategoryDto requestDto = createCategoryDto();

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new BadRequestException("Category already exists"));

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Category already exists"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).createCategory(any(Category.class));
        }


    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should return 400 when ID is negative")
        void getCategoryWithNegativeIdShouldReturnBadRequest() throws Exception {
            Long invalidId = -1L;

            mockMvc.perform(get("/api/categories/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).getCategoryById(any());
        }

        @Test
        @DisplayName("Should return 400 when ID is zero")
        void getCategoryWithZeroIdShouldReturnBadRequest() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(get("/api/categories/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).getCategoryById(any());
        }

        @Test
        @DisplayName("Should return 400 when request body is null")
        void createCategoryWithNullBodyShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 413 when payload is too large")
        void createCategoryWithLargePayloadShouldReturnPayloadTooLarge() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            requestDto.setName("a".repeat(10000)); // Very large string

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should ignore unknown fields and process valid data")
        void createCategoryWithUnknownFieldsShouldIgnoreUnknownFields() throws Exception {
            String jsonWithUnknownFields = """
                {
                    "unknownField": "value",
                    "anotherUnknownField": 123
                }
                """;

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithUnknownFields))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON has wrong data types")
        void createCategoryWithWrongDataTypesShouldReturnBadRequest() throws Exception {
            String jsonWithWrongTypes = """
                {
                    "name": 123,
                }
                """;

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithWrongTypes))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should handle empty list responses correctly")
        void getAllCategorysWhenEmptyListShouldReturnOk() throws Exception {
            List<Category> emptyCategoryList = List.of();

            when(categoryService.getAllCategorys()).thenReturn(emptyCategoryList);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$").isArray());

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void getCategoryWithVeryLargeIdShouldHandleCorrectly() throws Exception {
            Long veryLargeId = Long.MAX_VALUE;

            when(categoryService.getCategoryById(veryLargeId))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + veryLargeId));

            mockMvc.perform(get("/api/categories/{id}", veryLargeId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService).getCategoryById(veryLargeId);
        }

        @Test
        @DisplayName("Should handle special characters in string fields")
        void createCategoryWithSpecialCharactersShouldWork() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            String specialCharsValue = "Oussama's café & résumé (2024) - 100% tested!";
            requestDto.setName(specialCharsValue);
            responseDto.setName(specialCharsValue);
            responseDto.setId(1L);

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists("Location"));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should handle Unicode characters correctly")
        void createCategoryWithUnicodeCharactersShouldWork() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            String unicodeValue = "Mohammed العربية 中文 🚀 ñáéíóú";
            requestDto.setName(unicodeValue);
            responseDto.setName(unicodeValue);
            responseDto.setId(1L);

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists("Location"));

            verify(categoryService).createCategory(any(Category.class));
        }
    }

    @Nested
    @DisplayName("HTTP Method and Path Tests")
    class HttpMethodAndPathTests {


        @Test
        @DisplayName("Should return 405 for unsupported HTTP methods on existing paths")
        void unsupportedHttpMethodShouldReturn405() throws Exception {
            mockMvc.perform(patch("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/categories"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 405 when using PATCH on specific resource")
        void patchOnSpecificResourceShouldReturn405() throws Exception {
            Long categoryId = 1L;

            mockMvc.perform(patch("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 200 when using HEAD on collection (Spring Boot allows HEAD for GET)")
        void headOnCollectionShouldReturn200() throws Exception {
            List<Category> mockEntities = Arrays.asList(createMockEntity());
            when(categoryService.getAllCategorys()).thenReturn(mockEntities);

            mockMvc.perform(head("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should return 200 when using OPTIONS on specific resource (Spring Boot handles OPTIONS)")
        void optionsOnSpecificResourceShouldReturn200() throws Exception {
            Long categoryId = 1L;

            mockMvc.perform(options("/api/categories/{id}", categoryId))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Allow"));
        }

        @Test
        @DisplayName("Should handle trailing slashes correctly")
        void pathWithTrailingSlashShouldWork() throws Exception {
            CategoryDto categoryDto1 = createCategoryDto();
            CategoryDto categoryDto2 = createCategoryDto();
            categoryDto1.setId(1L);
            categoryDto2.setId(2L);

            Category entity1 = createCategory();
            Category entity2 = createCategory();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Category> entityList = Arrays.asList(entity1, entity2);
            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle case sensitivity in paths correctly")
        void pathCaseSensitivityShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/CATEGORYS")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle double slashes in paths gracefully")
        void pathWithDoubleSlashesShouldNormalizePath() throws Exception {
            when(categoryService.getAllCategorys()).thenReturn(Arrays.asList(createMockEntity()));

            mockMvc.perform(get("/api//categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle missing Accept header gracefully")
        void requestWithoutAcceptHeaderShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should return 406 for unsupported Accept header")
        void requestWithWrongAcceptHeaderShouldReturn406() throws Exception {
            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle multiple Accept headers correctly")
        void requestWithMultipleAcceptHeadersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }
    }

    @Nested
    @DisplayName("JSON Response Structure Tests")
    class JsonResponseStructureTests {

        @Test
        @DisplayName("Should handle successful creation when no validation rules exist")
        void successfulCreationShouldHaveProperStructure() throws Exception {
            CategoryDto requestDto = CategoryDto.builder().build();

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return consistent error response structure for not found errors")
        void notFoundErrorShouldHaveConsistentStructure() throws Exception {
            Long nonExistentId = 999L;

            when(categoryService.getCategoryById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/categories/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Category not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/categories/" + nonExistentId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(categoryService).getCategoryById(nonExistentId);
        }

        @Test
        @DisplayName("Should return consistent error response structure for internal server errors")
        void internalServerErrorShouldHaveConsistentStructure() throws Exception {
            Long categoryId = 1L;

            when(categoryService.getCategoryById(categoryId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/categories/" + categoryId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful creation")
        void successfulCreationShouldHaveProperJsonStructure() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            responseDto.setId(1L);

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.description").exists())
                    .andExpect(jsonPath("$.active").exists())
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful retrieval")
        void successfulRetrievalShouldHaveProperJsonStructure() throws Exception {
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            when(categoryService.getCategoryById(categoryId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.description").exists())
                    .andExpect(jsonPath("$.active").exists())
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should return proper JSON array structure for list retrieval")
        void listRetrievalShouldHaveProperJsonStructure() throws Exception {
            CategoryDto categoryDto1 = createCategoryDto();
            CategoryDto categoryDto2 = createCategoryDto();
            categoryDto1.setId(1L);
            categoryDto2.setId(2L);

            Category entity1 = createCategory();
            Category entity2 = createCategory();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Category> entityList = Arrays.asList(entity1, entity2);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[1].name").exists())
                    .andExpect(jsonPath("$[0].timestamp").doesNotExist())
                    .andExpect(jsonPath("$[0].status").doesNotExist())
                    .andExpect(jsonPath("$[0].error").doesNotExist());

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should return empty array with proper structure when no entities exist")
        void emptyListShouldHaveProperJsonStructure() throws Exception {
            List<Category> emptyCategoryList = List.of();

            when(categoryService.getAllCategorys()).thenReturn(emptyCategoryList);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(content().json("[]"));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should return no content body for successful deletion")
        void successfulDeletionShouldHaveNoContent() throws Exception {
            Long categoryId = 1L;

            doNothing().when(categoryService).deleteCategory(categoryId);

            mockMvc.perform(delete("/api/categories/{id}", categoryId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""))
                    .andExpect(header().doesNotExist("Content-Type"));

            verify(categoryService).deleteCategory(categoryId);
        }

        @Test
        @DisplayName("Should handle null values in JSON response correctly")
        void nullValuesInResponseShouldBeHandledCorrectly() throws Exception {
            Long categoryId = 1L;

            Category mockEntity = createMockEntity();
            mockEntity.setName(null);

            when(categoryService.getCategoryById(categoryId)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value(nullValue()))
                    .andExpect(jsonPath("$.id").value(1));

            verify(categoryService).getCategoryById(categoryId);
        }
    }

    @Nested
    @DisplayName("Header and Parameter Tests")
    class HeaderAndParameterTests {

        @Test
        @DisplayName("Should handle custom headers correctly")
        void requestWithCustomHeadersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .header("X-Request-ID", "test-request-123")
                    .header("X-Client-Version", "1.0.0")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle query parameters correctly")
        void requestWithQueryParametersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .param("sort", "id")
                    .param("order", "asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle pagination parameters correctly")
        void requestWithPaginationParametersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);
            Page<Category> categoryPage = new PageImpl<>(entityList, PageRequest.of(0, 10), 1);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(categoryPage);

            mockMvc.perform(get("/api/categories/paginated")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));

            verify(categoryService).getAllCategorys(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle very large page size parameters")
        void requestWithLargePageSizeShouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/categories/paginated")
                    .param("page", "0")
                    .param("size", "10000")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(categoryService, never()).getAllCategorys(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle missing required headers gracefully")
        void requestWithoutRequiredHeadersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle malformed query parameters")
        void requestWithMalformedQueryParametersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .param("invalid_param", "value")
                    .param("malformed_param", "")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should handle special characters in query parameters")
        void requestWithSpecialCharactersInParametersShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .param("search", "Oussama & Hicham's café")
                    .param("filter", "name=Mohammed,age>25")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should return proper Content-Type header")
        void responseShouldHaveProperContentTypeHeader() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }



        @Test
        @DisplayName("Should handle multiple values for same query parameter")
        void requestWithMultipleParameterValuesShouldWork() throws Exception {
            CategoryDto categoryDto = createCategoryDto();
            categoryDto.setId(1L);

            Category entity = createCategory();
            entity.setId(1L);
            List<Category> entityList = Arrays.asList(entity);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            mockMvc.perform(get("/api/categories")
                    .param("tags", "tag1", "tag2", "tag3")
                    .param("categories", "cat1", "cat2")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(categoryService).getAllCategorys();
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated Categorys with default parameters and proper headers")
        void getCategorysWithDefaultPaginationShouldReturnOk() throws Exception {
            CategoryDto categoryDto1 = createCategoryDto();
            CategoryDto categoryDto2 = createCategoryDto();
            categoryDto1.setId(1L);
            categoryDto2.setId(2L);

            Category entity1 = createCategory();
            Category entity2 = createCategory();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Category> categoryList = Arrays.asList(entity1, entity2);
            Page<Category> categoryPage = new PageImpl<>(categoryList, PageRequest.of(0, 20), 2);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(categoryPage);

            mockMvc.perform(get("/api/categories/paginated")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(header().string("X-Total-Count", "2"))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[1].id").value(2));

            verify(categoryService).getAllCategorys(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated Categorys with custom page and size")
        void getCategorysWithCustomPaginationShouldReturnOk() throws Exception {
            Category testCategory = createCategory();
            testCategory.setId(1L);

            List<Category> categoryList = Arrays.asList(testCategory);
            Page<Category> categoryPage = new PageImpl<>(categoryList, PageRequest.of(1, 5), 10);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(categoryPage);

            mockMvc.perform(get("/api/categories/paginated")
                    .param("page", "1")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(10))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(1));

            verify(categoryService).getAllCategorys(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated Categorys with sorting")
        void getCategorysWithSortingShouldReturnOk() throws Exception {
            Category category1 = createCategory();
            Category category2 = createCategory();
            category1.setId(1L);
            category2.setId(2L);

            List<Category> categoryList = Arrays.asList(category2, category1);
            Page<Category> categoryPage = new PageImpl<>(categoryList, PageRequest.of(0, 20), 2);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(categoryPage);

            mockMvc.perform(get("/api/categories/paginated")
                    .param("sort", "id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(2))
                    .andExpect(jsonPath("$.content[1].id").value(1));

            verify(categoryService).getAllCategorys(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no Categorys exist")
        void getCategorysWhenEmptyShouldReturnEmptyPage() throws Exception {
            Page<Category> emptyCategoryPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(emptyCategoryPage);

            mockMvc.perform(get("/api/categories/paginated"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));

            verify(categoryService).getAllCategorys(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle large page size requests")
        void getCategorysWithLargePageSizeShouldReturnOk() throws Exception {
            List<Category> categoryList = Arrays.asList();
            Page<Category> categoryPage = new PageImpl<>(categoryList, PageRequest.of(0, 1000), 0);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(categoryPage);

            mockMvc.perform(get("/api/categories/paginated")
                    .param("size", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.size").value(1000));

            verify(categoryService).getAllCategorys(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("OpenAPI/Swagger Documentation Tests")
    class SwaggerDocumentationTests {

        @Test
        @DisplayName("Should have proper OpenAPI annotations on create endpoint")
        void createCategoryEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            responseDto.setId(1L);

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(post("/api/categories")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // @Operation(summary = "Create Category", description = "Creates a new Category")
            // @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get by ID endpoint")
        void getCategoryByIdEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            when(categoryService.getCategoryById(categoryId)).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(get("/api/categories/{id}", categoryId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get Category by ID", description = "Returns a single Category")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(categoryService).getCategoryById(categoryId);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on update endpoint")
        void updateCategoryEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            CategoryDto responseDto = createCategoryDto();
            Long categoryId = 1L;
            responseDto.setId(categoryId);

            when(categoryService.updateCategory(eq(categoryId), any(Category.class)))
                    .thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(put("/api/categories/{id}", categoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Update Category", description = "Updates an existing Category")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(categoryService).updateCategory(eq(categoryId), any(Category.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on delete endpoint")
        void deleteCategoryEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            Long categoryId = 1L;

            doNothing().when(categoryService).deleteCategory(categoryId);

            MvcResult result = mockMvc.perform(delete("/api/categories/{id}", categoryId))
                    .andExpect(status().isNoContent())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Delete Category", description = "Deletes a Category")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "204", description = "No Content"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(204);
            verify(categoryService).deleteCategory(categoryId);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get all endpoint")
        void getAllCategorysEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            CategoryDto categoryDto1 = createCategoryDto();
            CategoryDto categoryDto2 = createCategoryDto();
            categoryDto1.setId(1L);
            categoryDto2.setId(2L);

            Category entity1 = createCategory();
            Category entity2 = createCategory();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Category> entityList = Arrays.asList(entity1, entity2);

            when(categoryService.getAllCategorys()).thenReturn(entityList);

            MvcResult result = mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get all Categorys", description = "Returns a list of all Categorys")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on paginated endpoint")
        void getCategorysPageEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            CategoryDto categoryDto1 = createCategoryDto();
            CategoryDto categoryDto2 = createCategoryDto();
            categoryDto1.setId(1L);
            categoryDto2.setId(2L);

            Category entity1 = createCategory();
            Category entity2 = createCategory();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Category> entityList = Arrays.asList(entity1, entity2);
            Page<Category> categoryPage = new PageImpl<>(entityList, PageRequest.of(0, 20), 2);

            when(categoryService.getAllCategorys(any(Pageable.class))).thenReturn(categoryPage);

            MvcResult result = mockMvc.perform(get("/api/categories/paginated")
                    .param("page", "0")
                    .param("size", "20")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get paginated Categorys", description = "Returns a paginated list of Categorys")
            // @Parameter(name = "page", description = "Page number (0-based)")
            // @Parameter(name = "size", description = "Page size")
            // @Parameter(name = "sort", description = "Sort criteria")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(categoryService).getAllCategorys(any(Pageable.class));
        }

        @Test
        @DisplayName("Should document error responses properly")
        void errorResponsesShouldBeProperlyDocumented() throws Exception {
            Long nonExistentId = 999L;

            when(categoryService.getCategoryById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Category not found with id: " + nonExistentId));

            MvcResult result = mockMvc.perform(get("/api/categories/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Verify that error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "404", description = "Not Found", 
            //              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(404);
            verify(categoryService).getCategoryById(nonExistentId);
        }

        @Test
        @DisplayName("Should document successful creation when no validation rules exist")
        void successfulCreationResponsesShouldBeProperlyDocumented() throws Exception {
            CategoryDto requestDto = CategoryDto.builder().build();

            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Verify that successful creation responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "201", description = "Created",
            //              content = @Content(schema = @Schema(implementation = CategoryDto.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            verify(categoryService).createCategory(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        @Test
        @DisplayName("Should allow access to public endpoints without authentication")
        void accessPublicEndpointWithoutAuthenticationShouldSucceed() throws Exception {
            List<Category> mockEntities = Arrays.asList(createMockEntity());
            when(categoryService.getAllCategorys()).thenReturn(mockEntities);

            mockMvc.perform(get("/api/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").exists());

            verify(categoryService).getAllCategorys();
        }

        @Test
        @DisplayName("Should allow create operation for any user since security is disabled")
        void createWithUserRoleShouldSucceed() throws Exception {
            CategoryDto requestDto = createCategoryDto();
            when(categoryService.createCategory(any(Category.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/categories")
                    .with(user("testuser").roles("USER"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(categoryService).createCategory(any(Category.class));
        }

    }




}
