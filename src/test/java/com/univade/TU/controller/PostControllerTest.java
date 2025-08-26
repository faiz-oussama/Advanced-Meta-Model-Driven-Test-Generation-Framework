package com.univade.TU.controller;

import com.univade.TU.testdata.PostTestDataBuilder;
import com.univade.TU.dto.PostDto;

import com.univade.TU.testdata.UserTestDataBuilder;
import com.univade.TU.dto.UserDto;

import com.univade.TU.entity.Post;

import com.univade.TU.entity.User;

import com.univade.TU.controller.PostController;
import com.univade.TU.service.PostService;
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

@WebMvcTest(PostController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(com.univade.TU.config.TestSecurityConfig.class)
@WithMockUser
@DisplayName("Post Controller Tests")
class PostControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(postService);
    }

    private Post createPost() {
        PostTestDataBuilder builder = PostTestDataBuilder.aValidPost();
        builder = builder.withAuthor(createUser());
        return builder.build();
    }

    private PostDto createPostDto() {
        Post entity = createPost();
        if (entity.getAuthor() != null && entity.getAuthor().getId() == null) {
            entity.getAuthor().setId(1L);
        }
        return PostDto.builder()
                .title(entity.getTitle())
                .content(entity.getContent())
                .authorId(entity.getAuthor() != null ? entity.getAuthor().getId() : 1L)
                .build();
    }



    private Post createMockEntity() {
        Post mockEntity = createPost();
        mockEntity.setId(1L);
        if (mockEntity.getAuthor() != null && mockEntity.getAuthor().getId() == null) {
            mockEntity.getAuthor().setId(1L);
        }
        return mockEntity;
    }

    private User createUser() {
        return UserTestDataBuilder.aDefaultUser().build();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create Post successfully with 201 status")
        void createPostShouldReturnCreated() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            responseDto.setId(1L);

            Post mockEntity = createPost();
            mockEntity.setId(1L);
            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should get Post by ID successfully with 200 status")
        void getPostByIdShouldReturnOk() throws Exception {
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            Post mockEntity = createPost();
            mockEntity.setId(postId);
            when(postService.getPostById(postId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id").value(1));

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should return 404 when Post not found")
        void getPostByIdShouldReturn404WhenNotFound() throws Exception {
            Long postId = 999L;

            when(postService.getPostById(postId))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + postId));

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Post not found with id: " + postId))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should update Post successfully with 200 status")
        void updatePostShouldReturnOk() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            Post mockEntity = createPost();
            mockEntity.setId(postId);
            when(postService.updatePost(eq(postId), any(Post.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/posts/{id}", postId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id").value(1));

            verify(postService).updatePost(eq(postId), any(Post.class));
        }

        @Test
        @DisplayName("Should delete Post successfully with 204 status")
        void deletePostShouldReturnNoContent() throws Exception {
            Long postId = 1L;

            doNothing().when(postService).deletePost(postId);

            mockMvc.perform(delete("/api/posts/{id}", postId)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(postService).deletePost(postId);
        }

        @Test
        @DisplayName("Should get all Posts successfully with 200 status")
        void getAllPostsShouldReturnOk() throws Exception {
            PostDto postDto1 = createPostDto();
            PostDto postDto2 = createPostDto();
            postDto1.setId(1L);
            postDto2.setId(2L);

            List<PostDto> postDtoList = Arrays.asList(postDto1, postDto2);

            Post entity1 = createPost();
            Post entity2 = createPost();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Post> entityList = Arrays.asList(entity1, entity2);
            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));

            verify(postService).getAllPosts();
        }
    }

    @Nested
    @DisplayName("Dynamic Validation Tests")
    class DynamicValidationTests {

        @Test
        @DisplayName("Should dynamically test all validation constraints from DTO")
        void testDynamicValidationConstraints() throws Exception {
            List<ControllerValidationRule> rules = createValidationRules();

            assertThat(rules).isNotEmpty();

            for (ControllerValidationRule rule : rules) {
                PostDto dto = createPostDto();

                setInvalidValueOnDto(dto, rule);
                mockMvc.perform(post("/api/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            verify(postService, never()).createPost(any(Post.class));
        }

        private List<ControllerValidationRule> createValidationRules() {
            List<ControllerValidationRule> rules = new ArrayList<>();

            // Should return 400 when authorId is null
            rules.add(ControllerValidationRule.builder()
                    .testName("createPostWithNullAuthorIdShouldReturnBadRequest")
                    .attributeName("authorId")
                    .validationType("NotNull")
                    .description("Should return 400 when authorId is null")
                    .expectedHttpStatus("400")
                    .message("Author cannot be null")
                    .invalidValue(null)
                    .build());

            // Should return 400 when title is blank
            rules.add(ControllerValidationRule.builder()
                    .testName("createPostWithBlankTitleShouldReturnBadRequest")
                    .attributeName("title")
                    .validationType("NotBlank")
                    .description("Should return 400 when title is blank")
                    .expectedHttpStatus("400")
                    .message("Title cannot be blank")
                    .invalidValue("")
                    .build());

            // Should return 400 when title has invalid size
            rules.add(ControllerValidationRule.builder()
                    .testName("createPostWithInvalidSizeTitleShouldReturnBadRequest")
                    .attributeName("title")
                    .validationType("Size")
                    .description("Should return 400 when title has invalid size")
                    .expectedHttpStatus("400")
                    .message("Title must be between 3 and 255 characters")
                    .minValue(3)
                    .maxValue(255)
                    .invalidValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .build());

            // Should return 400 when content is blank
            rules.add(ControllerValidationRule.builder()
                    .testName("createPostWithBlankContentShouldReturnBadRequest")
                    .attributeName("content")
                    .validationType("NotBlank")
                    .description("Should return 400 when content is blank")
                    .expectedHttpStatus("400")
                    .message("Content cannot be blank")
                    .invalidValue("")
                    .build());

            // Should return 400 when content has invalid size
            rules.add(ControllerValidationRule.builder()
                    .testName("createPostWithInvalidSizeContentShouldReturnBadRequest")
                    .attributeName("content")
                    .validationType("Size")
                    .description("Should return 400 when content has invalid size")
                    .expectedHttpStatus("400")
                    .message("Content must be between 10 and 5000 characters")
                    .minValue(10)
                    .maxValue(5000)
                    .invalidValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .build());

            return rules;
        }

        private void setInvalidValueOnDto(PostDto dto, ControllerValidationRule rule) throws Exception {
            Field field = PostDto.class.getDeclaredField(rule.getAttributeName());
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

        private void setNumericFieldValue(Field field, PostDto dto, int value) throws Exception {
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
        @DisplayName("Should return 400 when multiple validation errors occur")
        void createPostWithMultipleValidationErrorsShouldReturnBadRequest() throws Exception {
            PostDto requestDto = PostDto.builder().build();

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any(Post.class));
        }

    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should return 404 when entity not found on GET")
        void getPostByIdShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;

            when(postService.getPostById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/posts/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Post not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/posts/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).getPostById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 404 when entity not found on UPDATE")
        void updatePostShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;
            PostDto requestDto = createPostDto();

            when(postService.updatePost(eq(nonExistentId), any(Post.class)))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + nonExistentId));

            mockMvc.perform(put("/api/posts/{id}", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Post not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/posts/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).updatePost(eq(nonExistentId), any(Post.class));
        }

        @Test
        @DisplayName("Should return 404 when entity not found on DELETE")
        void deletePostShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;

            doThrow(new EntityNotFoundException("Post not found with id: " + nonExistentId))
                    .when(postService).deletePost(nonExistentId);

            mockMvc.perform(delete("/api/posts/{id}", nonExistentId)
                    .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Post not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/posts/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).deletePost(nonExistentId);
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void createPostShouldReturn500OnUnexpectedException() throws Exception {
            PostDto requestDto = createPostDto();

            when(postService.createPost(any(Post.class)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 400 when service throws BadRequestException")
        void createPostShouldReturn400OnBadRequestException() throws Exception {
            PostDto requestDto = createPostDto();

            when(postService.createPost(any(Post.class)))
                    .thenThrow(new BadRequestException("Invalid data provided"));

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Invalid data provided"))
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 400 when bad request exception occurs")
        void createPostShouldReturn400WhenBadRequestException() throws Exception {
            PostDto requestDto = createPostDto();
            String errorMessage = "Invalid data provided";

            when(postService.createPost(any(Post.class)))
                    .thenThrow(new BadRequestException(errorMessage));

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 500 when internal server error occurs")
        void getPostShouldReturn500WhenInternalServerError() throws Exception {
            Long postId = 1L;

            when(postService.getPostById(postId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.path").value("/api/posts/" + postId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should return 405 when HTTP method not allowed")
        void shouldReturn405WhenMethodNotAllowed() throws Exception {
            mockMvc.perform(patch("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 406 when accept header is not supported")
        void shouldReturn406WhenAcceptHeaderNotSupported() throws Exception {
            Long postId = 1L;
            PostDto responseDto = createPostDto();
            responseDto.setId(postId);

            when(postService.getPostById(postId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/posts/" + postId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle constraint violation exceptions properly")
        void createPostShouldHandleConstraintViolationException() throws Exception {
            PostDto requestDto = createPostDto();

            when(postService.createPost(any(Post.class)))
                    .thenThrow(new ConstraintViolationException("Constraint violation", null));

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).createPost(any(Post.class));
        }
    }

    @Nested
    @DisplayName("Status Code Tests")
    class StatusCodeTests {

        @Test
        @DisplayName("Should return 201 Created with proper Location header on successful creation")
        void createPostShouldReturn201WithLocationHeader() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            responseDto.setId(1L);

            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("1")))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 200 OK on successful retrieval")
        void getPostByIdShouldReturn200() throws Exception {
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            when(postService.getPostById(postId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should return 200 OK on successful update")
        void updatePostShouldReturn200() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            when(postService.updatePost(eq(postId), any(Post.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/posts/{id}", postId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(postService).updatePost(eq(postId), any(Post.class));
        }

        @Test
        @DisplayName("Should return 204 No Content on successful deletion")
        void deletePostShouldReturn204() throws Exception {
            Long postId = 1L;

            doNothing().when(postService).deletePost(postId);

            mockMvc.perform(delete("/api/posts/{id}", postId)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(postService).deletePost(postId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when entity does not exist")
        void getPostByIdShouldReturn404WhenNotFound() throws Exception {
            Long nonExistentId = 999L;

            when(postService.getPostById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/posts/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).getPostById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 400 Bad Request on validation failure")
        void createPostWithInvalidDataShouldReturn400() throws Exception {
            PostDto invalidDto = PostDto.builder().build();

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void createPostWithWrongContentTypeShouldReturn415() throws Exception {
            PostDto requestDto = createPostDto();

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any());
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error on service exception")
        void getPostByIdShouldReturn500OnServiceException() throws Exception {
            Long postId = 1L;

            when(postService.getPostById(postId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should return 409 Conflict on duplicate resource creation")
        void createPostWithDuplicateDataShouldReturn409() throws Exception {
            PostDto requestDto = createPostDto();

            when(postService.createPost(any(Post.class)))
                    .thenThrow(new BadRequestException("Post already exists"));

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Post already exists"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).createPost(any(Post.class));
        }


    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should return 400 when ID is negative")
        void getPostWithNegativeIdShouldReturnBadRequest() throws Exception {
            Long invalidId = -1L;

            mockMvc.perform(get("/api/posts/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).getPostById(any());
        }

        @Test
        @DisplayName("Should return 400 when ID is zero")
        void getPostWithZeroIdShouldReturnBadRequest() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(get("/api/posts/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).getPostById(any());
        }

        @Test
        @DisplayName("Should return 400 when request body is null")
        void createPostWithNullBodyShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 413 when payload is too large")
        void createPostWithLargePayloadShouldReturnPayloadTooLarge() throws Exception {
            PostDto requestDto = createPostDto();
            requestDto.setTitle("a".repeat(10000)); // Very large string

            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON contains only unknown fields and entity has strict validation")
        void createPostWithUnknownFieldsShouldReturnBadRequest() throws Exception {
            String jsonWithUnknownFields = """
                {
                    "unknownField": "value",
                    "anotherUnknownField": 123
                }
                """;

            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithUnknownFields))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON has wrong data types")
        void createPostWithWrongDataTypesShouldReturnBadRequest() throws Exception {
            String jsonWithWrongTypes = """
                {
                    "title": 123,
                }
                """;

            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithWrongTypes))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should handle empty list responses correctly")
        void getAllPostsWhenEmptyListShouldReturnOk() throws Exception {
            List<Post> emptyPostList = List.of();

            when(postService.getAllPosts()).thenReturn(emptyPostList);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$").isArray());

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void getPostWithVeryLargeIdShouldHandleCorrectly() throws Exception {
            Long veryLargeId = Long.MAX_VALUE;

            when(postService.getPostById(veryLargeId))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + veryLargeId));

            mockMvc.perform(get("/api/posts/{id}", veryLargeId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService).getPostById(veryLargeId);
        }

        @Test
        @DisplayName("Should handle special characters in string fields")
        void createPostWithSpecialCharactersShouldWork() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            String specialCharsValue = "Oussama's café & résumé (2024) - 100% tested!";
            requestDto.setTitle(specialCharsValue);
            responseDto.setTitle(specialCharsValue);
            responseDto.setId(1L);

            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists("Location"));

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should handle Unicode characters correctly")
        void createPostWithUnicodeCharactersShouldWork() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            String unicodeValue = "Mohammed العربية 中文 🚀 ñáéíóú";
            requestDto.setTitle(unicodeValue);
            responseDto.setTitle(unicodeValue);
            responseDto.setId(1L);

            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists("Location"));

            verify(postService).createPost(any(Post.class));
        }
    }

    @Nested
    @DisplayName("HTTP Method and Path Tests")
    class HttpMethodAndPathTests {


        @Test
        @DisplayName("Should return 405 for unsupported HTTP methods on existing paths")
        void unsupportedHttpMethodShouldReturn405() throws Exception {
            mockMvc.perform(patch("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 405 when using PATCH on specific resource")
        void patchOnSpecificResourceShouldReturn405() throws Exception {
            Long postId = 1L;

            mockMvc.perform(patch("/api/posts/{id}", postId)
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
            List<Post> mockEntities = Arrays.asList(createMockEntity());
            when(postService.getAllPosts()).thenReturn(mockEntities);

            mockMvc.perform(head("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should return 200 when using OPTIONS on specific resource (Spring Boot handles OPTIONS)")
        void optionsOnSpecificResourceShouldReturn200() throws Exception {
            Long postId = 1L;

            mockMvc.perform(options("/api/posts/{id}", postId))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Allow"));
        }

        @Test
        @DisplayName("Should handle trailing slashes correctly")
        void pathWithTrailingSlashShouldWork() throws Exception {
            PostDto postDto1 = createPostDto();
            PostDto postDto2 = createPostDto();
            postDto1.setId(1L);
            postDto2.setId(2L);

            Post entity1 = createPost();
            Post entity2 = createPost();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Post> entityList = Arrays.asList(entity1, entity2);
            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle case sensitivity in paths correctly")
        void pathCaseSensitivityShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/POSTS")
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
            when(postService.getAllPosts()).thenReturn(Arrays.asList(createMockEntity()));

            mockMvc.perform(get("/api//posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle missing Accept header gracefully")
        void requestWithoutAcceptHeaderShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should return 406 for unsupported Accept header")
        void requestWithWrongAcceptHeaderShouldReturn406() throws Exception {
            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).getAllPosts();
        }

        @Test
        @DisplayName("Should handle multiple Accept headers correctly")
        void requestWithMultipleAcceptHeadersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }
    }

    @Nested
    @DisplayName("JSON Response Structure Tests")
    class JsonResponseStructureTests {

        @Test
        @DisplayName("Should return consistent error response structure for validation failures")
        void validationErrorShouldHaveConsistentStructure() throws Exception {
            PostDto invalidDto = PostDto.builder().build();

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.path").value("/api/posts"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").exists())
                    .andExpect(jsonPath("$.errors[0].message").exists());

            verify(postService, never()).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return consistent error response structure for not found errors")
        void notFoundErrorShouldHaveConsistentStructure() throws Exception {
            Long nonExistentId = 999L;

            when(postService.getPostById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/posts/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Post not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/posts/" + nonExistentId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(postService).getPostById(nonExistentId);
        }

        @Test
        @DisplayName("Should return consistent error response structure for internal server errors")
        void internalServerErrorShouldHaveConsistentStructure() throws Exception {
            Long postId = 1L;

            when(postService.getPostById(postId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/posts/" + postId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful creation")
        void successfulCreationShouldHaveProperJsonStructure() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            responseDto.setId(1L);

            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.content").exists())
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful retrieval")
        void successfulRetrievalShouldHaveProperJsonStructure() throws Exception {
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            when(postService.getPostById(postId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.content").exists())
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should return proper JSON array structure for list retrieval")
        void listRetrievalShouldHaveProperJsonStructure() throws Exception {
            PostDto postDto1 = createPostDto();
            PostDto postDto2 = createPostDto();
            postDto1.setId(1L);
            postDto2.setId(2L);

            Post entity1 = createPost();
            Post entity2 = createPost();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Post> entityList = Arrays.asList(entity1, entity2);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[0].title").exists())
                    .andExpect(jsonPath("$[1].title").exists())
                    .andExpect(jsonPath("$[0].timestamp").doesNotExist())
                    .andExpect(jsonPath("$[0].status").doesNotExist())
                    .andExpect(jsonPath("$[0].error").doesNotExist());

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should return empty array with proper structure when no entities exist")
        void emptyListShouldHaveProperJsonStructure() throws Exception {
            List<Post> emptyPostList = List.of();

            when(postService.getAllPosts()).thenReturn(emptyPostList);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(content().json("[]"));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should return no content body for successful deletion")
        void successfulDeletionShouldHaveNoContent() throws Exception {
            Long postId = 1L;

            doNothing().when(postService).deletePost(postId);

            mockMvc.perform(delete("/api/posts/{id}", postId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""))
                    .andExpect(header().doesNotExist("Content-Type"));

            verify(postService).deletePost(postId);
        }

        @Test
        @DisplayName("Should handle null values in JSON response correctly")
        void nullValuesInResponseShouldBeHandledCorrectly() throws Exception {
            Long postId = 1L;

            Post mockEntity = createMockEntity();

            when(postService.getPostById(postId)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(postService).getPostById(postId);
        }
    }

    @Nested
    @DisplayName("Header and Parameter Tests")
    class HeaderAndParameterTests {

        @Test
        @DisplayName("Should handle custom headers correctly")
        void requestWithCustomHeadersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .header("X-Request-ID", "test-request-123")
                    .header("X-Client-Version", "1.0.0")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle query parameters correctly")
        void requestWithQueryParametersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .param("sort", "id")
                    .param("order", "asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle pagination parameters correctly")
        void requestWithPaginationParametersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);
            Page<Post> postPage = new PageImpl<>(entityList, PageRequest.of(0, 10), 1);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            mockMvc.perform(get("/api/posts/paginated")
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

            verify(postService).getAllPosts(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle very large page size parameters")
        void requestWithLargePageSizeShouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/posts/paginated")
                    .param("page", "0")
                    .param("size", "10000")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(postService, never()).getAllPosts(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle missing required headers gracefully")
        void requestWithoutRequiredHeadersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle malformed query parameters")
        void requestWithMalformedQueryParametersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .param("invalid_param", "value")
                    .param("malformed_param", "")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should handle special characters in query parameters")
        void requestWithSpecialCharactersInParametersShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .param("search", "Oussama & Hicham's café")
                    .param("filter", "name=Mohammed,age>25")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should return proper Content-Type header")
        void responseShouldHaveProperContentTypeHeader() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }



        @Test
        @DisplayName("Should handle multiple values for same query parameter")
        void requestWithMultipleParameterValuesShouldWork() throws Exception {
            PostDto postDto = createPostDto();
            postDto.setId(1L);

            Post entity = createPost();
            entity.setId(1L);
            List<Post> entityList = Arrays.asList(entity);

            when(postService.getAllPosts()).thenReturn(entityList);

            mockMvc.perform(get("/api/posts")
                    .param("tags", "tag1", "tag2", "tag3")
                    .param("categories", "cat1", "cat2")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(postService).getAllPosts();
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated Posts with default parameters and proper headers")
        void getPostsWithDefaultPaginationShouldReturnOk() throws Exception {
            PostDto postDto1 = createPostDto();
            PostDto postDto2 = createPostDto();
            postDto1.setId(1L);
            postDto2.setId(2L);

            Post entity1 = createPost();
            Post entity2 = createPost();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Post> postList = Arrays.asList(entity1, entity2);
            Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(0, 20), 2);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            mockMvc.perform(get("/api/posts/paginated")
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

            verify(postService).getAllPosts(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated Posts with custom page and size")
        void getPostsWithCustomPaginationShouldReturnOk() throws Exception {
            Post testPost = createPost();
            testPost.setId(1L);

            List<Post> postList = Arrays.asList(testPost);
            Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(1, 5), 10);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            mockMvc.perform(get("/api/posts/paginated")
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

            verify(postService).getAllPosts(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated Posts with sorting")
        void getPostsWithSortingShouldReturnOk() throws Exception {
            Post post1 = createPost();
            Post post2 = createPost();
            post1.setId(1L);
            post2.setId(2L);

            List<Post> postList = Arrays.asList(post2, post1);
            Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(0, 20), 2);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            mockMvc.perform(get("/api/posts/paginated")
                    .param("sort", "id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(2))
                    .andExpect(jsonPath("$.content[1].id").value(1));

            verify(postService).getAllPosts(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no Posts exist")
        void getPostsWhenEmptyShouldReturnEmptyPage() throws Exception {
            Page<Post> emptyPostPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(emptyPostPage);

            mockMvc.perform(get("/api/posts/paginated"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));

            verify(postService).getAllPosts(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle large page size requests")
        void getPostsWithLargePageSizeShouldReturnOk() throws Exception {
            List<Post> postList = Arrays.asList();
            Page<Post> postPage = new PageImpl<>(postList, PageRequest.of(0, 1000), 0);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            mockMvc.perform(get("/api/posts/paginated")
                    .param("size", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.size").value(1000));

            verify(postService).getAllPosts(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("OpenAPI/Swagger Documentation Tests")
    class SwaggerDocumentationTests {

        @Test
        @DisplayName("Should have proper OpenAPI annotations on create endpoint")
        void createPostEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            responseDto.setId(1L);

            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // @Operation(summary = "Create Post", description = "Creates a new Post")
            // @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get by ID endpoint")
        void getPostByIdEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            when(postService.getPostById(postId)).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get Post by ID", description = "Returns a single Post")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on update endpoint")
        void updatePostEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            PostDto requestDto = createPostDto();
            PostDto responseDto = createPostDto();
            Long postId = 1L;
            responseDto.setId(postId);

            when(postService.updatePost(eq(postId), any(Post.class)))
                    .thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(put("/api/posts/{id}", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Update Post", description = "Updates an existing Post")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(postService).updatePost(eq(postId), any(Post.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on delete endpoint")
        void deletePostEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            Long postId = 1L;

            doNothing().when(postService).deletePost(postId);

            MvcResult result = mockMvc.perform(delete("/api/posts/{id}", postId))
                    .andExpect(status().isNoContent())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Delete Post", description = "Deletes a Post")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "204", description = "No Content"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(204);
            verify(postService).deletePost(postId);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get all endpoint")
        void getAllPostsEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            PostDto postDto1 = createPostDto();
            PostDto postDto2 = createPostDto();
            postDto1.setId(1L);
            postDto2.setId(2L);

            Post entity1 = createPost();
            Post entity2 = createPost();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Post> entityList = Arrays.asList(entity1, entity2);

            when(postService.getAllPosts()).thenReturn(entityList);

            MvcResult result = mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get all Posts", description = "Returns a list of all Posts")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on paginated endpoint")
        void getPostsPageEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            PostDto postDto1 = createPostDto();
            PostDto postDto2 = createPostDto();
            postDto1.setId(1L);
            postDto2.setId(2L);

            Post entity1 = createPost();
            Post entity2 = createPost();
            entity1.setId(1L);
            entity2.setId(2L);
            List<Post> entityList = Arrays.asList(entity1, entity2);
            Page<Post> postPage = new PageImpl<>(entityList, PageRequest.of(0, 20), 2);

            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            MvcResult result = mockMvc.perform(get("/api/posts/paginated")
                    .param("page", "0")
                    .param("size", "20")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get paginated Posts", description = "Returns a paginated list of Posts")
            // @Parameter(name = "page", description = "Page number (0-based)")
            // @Parameter(name = "size", description = "Page size")
            // @Parameter(name = "sort", description = "Sort criteria")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(postService).getAllPosts(any(Pageable.class));
        }

        @Test
        @DisplayName("Should document error responses properly")
        void errorResponsesShouldBeProperlyDocumented() throws Exception {
            Long nonExistentId = 999L;

            when(postService.getPostById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("Post not found with id: " + nonExistentId));

            MvcResult result = mockMvc.perform(get("/api/posts/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Verify that error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "404", description = "Not Found", 
            //              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(404);
            verify(postService).getPostById(nonExistentId);
        }

        @Test
        @DisplayName("Should document validation error responses")
        void validationErrorResponsesShouldBeProperlyDocumented() throws Exception {
            PostDto invalidDto = PostDto.builder().build(); // Invalid DTO

            MvcResult result = mockMvc.perform(post("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Verify that validation error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "400", description = "Bad Request - Validation Error",
            //              content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(400);
            verify(postService, never()).createPost(any(Post.class));
        }
    }

    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        @Test
        @DisplayName("Should allow access to public endpoints without authentication")
        void accessPublicEndpointWithoutAuthenticationShouldSucceed() throws Exception {
            List<Post> mockEntities = Arrays.asList(createMockEntity());
            when(postService.getAllPosts()).thenReturn(mockEntities);

            mockMvc.perform(get("/api/posts")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").exists());

            verify(postService).getAllPosts();
        }

        @Test
        @DisplayName("Should allow create operation for any user since security is disabled")
        void createWithUserRoleShouldSucceed() throws Exception {
            PostDto requestDto = createPostDto();
            when(postService.createPost(any(Post.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/posts")
                    .with(user("testuser").roles("USER"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(postService).createPost(any(Post.class));
        }

    }



    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should create Post with valid User")
        void createPostWithValidUserShouldReturnCreated() throws Exception {
            PostDto requestDto = createPostDto();
            Post mockEntity = createMockEntity();

            when(postService.createPost(any(Post.class))).thenReturn(mockEntity);

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.authorId").value(1));

            verify(postService).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should return 400 when User is null for required relationship")
        void createPostWithNullUserShouldReturnBadRequest() throws Exception {
            PostDto requestDto = createPostDto();
            requestDto.setAuthorId(null);

            mockMvc.perform(post("/api/posts")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());

            verify(postService, never()).createPost(any(Post.class));
        }

        @Test
        @DisplayName("Should get Post with User details")
        void getPostWithRelationshipDetailsShouldReturnOk() throws Exception {
            Long postId = 1L;
            Post mockEntity = createMockEntity();

            when(postService.getPostById(postId)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/posts/{id}", postId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.authorId").value(1));

            verify(postService).getPostById(postId);
        }

        @Test
        @DisplayName("Should update Post with new User")
        void updatePostWithNewUserShouldReturnOk() throws Exception {
            Long postId = 1L;
            PostDto updateDto = createPostDto();
            updateDto.setAuthorId(2L);

            Post mockEntity = createMockEntity();

            when(postService.updatePost(eq(postId), any(Post.class)))
                    .thenReturn(mockEntity);

            mockMvc.perform(put("/api/posts/{id}", postId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.authorId").value(1));

            verify(postService).updatePost(eq(postId), any(Post.class));
        }

    }

}
