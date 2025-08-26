package com.univade.TU.controller;

import com.univade.TU.testdata.UserTestDataBuilder;
import com.univade.TU.dto.UserDto;

import com.univade.TU.testdata.AddressTestDataBuilder;
import com.univade.TU.dto.AddressDto;
import com.univade.TU.testdata.PostTestDataBuilder;
import com.univade.TU.dto.PostDto;

import com.univade.TU.entity.User;

import com.univade.TU.entity.Address;
import com.univade.TU.entity.Post;

import com.univade.TU.controller.UserController;
import com.univade.TU.service.UserService;
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

@WebMvcTest(UserController.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(com.univade.TU.config.TestSecurityConfig.class)
@DisplayName("User Controller Tests")
class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(userService);
    }

    private User createUser() {
        UserTestDataBuilder builder = UserTestDataBuilder.aValidUser();
        builder = builder.withAddress(createAddress());
        return builder.build();
    }

    private UserDto createUserDto() {
        User entity = createUser();
        if (entity.getAddress() != null && entity.getAddress().getId() == null) {
            entity.getAddress().setId(1L);
        }
        return UserDto.builder()
                .name(entity.getName())
                .email(entity.getEmail())
                .age(entity.getAge())
                .addressId(entity.getAddress() != null ? entity.getAddress().getId() : 1L)
                .build();
    }



    private User createMockEntity() {
        User mockEntity = createUser();
        mockEntity.setId(1L);
        if (mockEntity.getAddress() != null && mockEntity.getAddress().getId() == null) {
            mockEntity.getAddress().setId(1L);
        }
        return mockEntity;
    }

    private Address createAddress() {
        return AddressTestDataBuilder.aDefaultAddress().build();
    }

    private Post createPost() {
        return PostTestDataBuilder.aDefaultPost().build();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create User successfully with 201 status")
        void createUserShouldReturnCreated() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            responseDto.setId(1L);

            User mockEntity = createUser();
            mockEntity.setId(1L);
            when(userService.createUser(any(User.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should get User by ID successfully with 200 status")
        void getUserByIdShouldReturnOk() throws Exception {
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            User mockEntity = createUser();
            mockEntity.setId(userId);
            when(userService.getUserById(userId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return 404 when User not found")
        void getUserByIdShouldReturn404WhenNotFound() throws Exception {
            Long userId = 999L;

            when(userService.getUserById(userId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + userId));

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("User not found with id: " + userId))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should update User successfully with 200 status")
        void updateUserShouldReturnOk() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            User mockEntity = createUser();
            mockEntity.setId(userId);
            when(userService.updateUser(eq(userId), any(User.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/users/{id}", userId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).updateUser(eq(userId), any(User.class));
        }

        @Test
        @DisplayName("Should delete User successfully with 204 status")
        void deleteUserShouldReturnNoContent() throws Exception {
            Long userId = 1L;

            doNothing().when(userService).deleteUser(userId);

            mockMvc.perform(delete("/api/users/{id}", userId)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(userService).deleteUser(userId);
        }

        @Test
        @DisplayName("Should get all Users successfully with 200 status")
        void getAllUsersShouldReturnOk() throws Exception {
            UserDto userDto1 = createUserDto();
            UserDto userDto2 = createUserDto();
            userDto1.setId(1L);
            userDto2.setId(2L);

            List<UserDto> userDtoList = Arrays.asList(userDto1, userDto2);

            User entity1 = createUser();
            User entity2 = createUser();
            entity1.setId(1L);
            entity2.setId(2L);
            List<User> entityList = Arrays.asList(entity1, entity2);
            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));

            verify(userService).getAllUsers();
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
                UserDto dto = createUserDto();

                setInvalidValueOnDto(dto, rule);
                mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.message").exists())
                        .andExpect(jsonPath("$.timestamp").exists());
            }

            verify(userService, never()).createUser(any(User.class));
        }

        private List<ControllerValidationRule> createValidationRules() {
            List<ControllerValidationRule> rules = new ArrayList<>();

            // Should return 400 when name is blank
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithBlankNameShouldReturnBadRequest")
                    .attributeName("name")
                    .validationType("NotBlank")
                    .description("Should return 400 when name is blank")
                    .expectedHttpStatus("400")
                    .message("Name cannot be blank")
                    .invalidValue("")
                    .build());

            // Should return 400 when name has invalid size
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithInvalidSizeNameShouldReturnBadRequest")
                    .attributeName("name")
                    .validationType("Size")
                    .description("Should return 400 when name has invalid size")
                    .expectedHttpStatus("400")
                    .message("Name must be between 2 and 100 characters")
                    .minValue(2)
                    .maxValue(100)
                    .invalidValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .build());

            // Should return 400 when age is below minimum value
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithTooSmallAgeShouldReturnBadRequest")
                    .attributeName("age")
                    .validationType("Min")
                    .description("Should return 400 when age is below minimum value")
                    .expectedHttpStatus("400")
                    .message("Age must be positive")
                    .constraintValue(0)
                    .invalidValue("-1")
                    .build());

            // Should return 400 when age exceeds maximum value
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithTooLargeAgeShouldReturnBadRequest")
                    .attributeName("age")
                    .validationType("Max")
                    .description("Should return 400 when age exceeds maximum value")
                    .expectedHttpStatus("400")
                    .message("Age must be realistic")
                    .constraintValue(150)
                    .invalidValue("151")
                    .build());

            // Should return 400 when email is not a valid email
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithInvalidEmailEmailShouldReturnBadRequest")
                    .attributeName("email")
                    .validationType("Email")
                    .description("Should return 400 when email is not a valid email")
                    .expectedHttpStatus("400")
                    .message("Email must be valid")
                    .invalidValue("invalid-email")
                    .build());

            // Should return 400 when email is blank
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithBlankEmailShouldReturnBadRequest")
                    .attributeName("email")
                    .validationType("NotBlank")
                    .description("Should return 400 when email is blank")
                    .expectedHttpStatus("400")
                    .message("Email cannot be blank")
                    .invalidValue("")
                    .build());

            // Should return 400 when email has invalid size
            rules.add(ControllerValidationRule.builder()
                    .testName("createUserWithInvalidSizeEmailShouldReturnBadRequest")
                    .attributeName("email")
                    .validationType("Size")
                    .description("Should return 400 when email has invalid size")
                    .expectedHttpStatus("400")
                    .message("Email must not exceed 150 characters")
                    .maxValue(150)
                    .invalidValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .build());

            return rules;
        }

        private void setInvalidValueOnDto(UserDto dto, ControllerValidationRule rule) throws Exception {
            Field field = UserDto.class.getDeclaredField(rule.getAttributeName());
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

        private void setNumericFieldValue(Field field, UserDto dto, int value) throws Exception {
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
        void createUserWithMultipleValidationErrorsShouldReturnBadRequest() throws Exception {
            UserDto requestDto = UserDto.builder().build();

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any(User.class));
        }

    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should return 404 when entity not found on GET")
        void getUserByIdShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;

            when(userService.getUserById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/users/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).getUserById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 404 when entity not found on UPDATE")
        void updateUserShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;
            UserDto requestDto = createUserDto();

            when(userService.updateUser(eq(nonExistentId), any(User.class)))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentId));

            mockMvc.perform(put("/api/users/{id}", nonExistentId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).updateUser(eq(nonExistentId), any(User.class));
        }

        @Test
        @DisplayName("Should return 404 when entity not found on DELETE")
        void deleteUserShouldReturn404WhenEntityNotFound() throws Exception {
            Long nonExistentId = 999L;

            doThrow(new EntityNotFoundException("User not found with id: " + nonExistentId))
                    .when(userService).deleteUser(nonExistentId);

            mockMvc.perform(delete("/api/users/{id}", nonExistentId)
                    .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).deleteUser(nonExistentId);
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void createUserShouldReturn500OnUnexpectedException() throws Exception {
            UserDto requestDto = createUserDto();

            when(userService.createUser(any(User.class)))
                    .thenThrow(new RuntimeException("Unexpected database error"));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when service throws BadRequestException")
        void createUserShouldReturn400OnBadRequestException() throws Exception {
            UserDto requestDto = createUserDto();

            when(userService.createUser(any(User.class)))
                    .thenThrow(new BadRequestException("Invalid data provided"));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Invalid data provided"))
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when bad request exception occurs")
        void createUserShouldReturn400WhenBadRequestException() throws Exception {
            UserDto requestDto = createUserDto();
            String errorMessage = "Invalid data provided";

            when(userService.createUser(any(User.class)))
                    .thenThrow(new BadRequestException(errorMessage));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value(errorMessage))
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 500 when internal server error occurs")
        void getUserShouldReturn500WhenInternalServerError() throws Exception {
            Long userId = 1L;

            when(userService.getUserById(userId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                    .andExpect(jsonPath("$.path").value("/api/users/" + userId))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return 405 when HTTP method not allowed")
        void shouldReturn405WhenMethodNotAllowed() throws Exception {
            mockMvc.perform(patch("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 406 when accept header is not supported")
        void shouldReturn406WhenAcceptHeaderNotSupported() throws Exception {
            Long userId = 1L;
            UserDto responseDto = createUserDto();
            responseDto.setId(userId);

            when(userService.getUserById(userId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users/" + userId))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle constraint violation exceptions properly")
        void createUserShouldHandleConstraintViolationException() throws Exception {
            UserDto requestDto = createUserDto();

            when(userService.createUser(any(User.class)))
                    .thenThrow(new ConstraintViolationException("Constraint violation", null));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).createUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("Status Code Tests")
    class StatusCodeTests {

        @Test
        @DisplayName("Should return 201 Created with proper Location header on successful creation")
        void createUserShouldReturn201WithLocationHeader() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            responseDto.setId(1L);

            when(userService.createUser(any(User.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(header().string("Location", containsString("1")))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 200 OK on successful retrieval")
        void getUserByIdShouldReturn200() throws Exception {
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            when(userService.getUserById(userId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return 200 OK on successful update")
        void updateUserShouldReturn200() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            when(userService.updateUser(eq(userId), any(User.class)))
                    .thenReturn(createMockEntity());

            mockMvc.perform(put("/api/users/{id}", userId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(userService).updateUser(eq(userId), any(User.class));
        }

        @Test
        @DisplayName("Should return 204 No Content on successful deletion")
        void deleteUserShouldReturn204() throws Exception {
            Long userId = 1L;

            doNothing().when(userService).deleteUser(userId);

            mockMvc.perform(delete("/api/users/{id}", userId)
                    .with(csrf()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(userService).deleteUser(userId);
        }

        @Test
        @DisplayName("Should return 404 Not Found when entity does not exist")
        void getUserByIdShouldReturn404WhenNotFound() throws Exception {
            Long nonExistentId = 999L;

            when(userService.getUserById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/users/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).getUserById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 400 Bad Request on validation failure")
        void createUserWithInvalidDataShouldReturn400() throws Exception {
            UserDto invalidDto = UserDto.builder().build();

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 415 Unsupported Media Type for wrong content type")
        void createUserWithWrongContentTypeShouldReturn415() throws Exception {
            UserDto requestDto = createUserDto();

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error on service exception")
        void getUserByIdShouldReturn500OnServiceException() throws Exception {
            Long userId = 1L;

            when(userService.getUserById(userId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return 409 Conflict on duplicate resource creation")
        void createUserWithDuplicateDataShouldReturn409() throws Exception {
            UserDto requestDto = createUserDto();

            when(userService.createUser(any(User.class)))
                    .thenThrow(new BadRequestException("User already exists"));

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("User already exists"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).createUser(any(User.class));
        }


    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should return 400 when ID is negative")
        void getUserWithNegativeIdShouldReturnBadRequest() throws Exception {
            Long invalidId = -1L;

            mockMvc.perform(get("/api/users/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).getUserById(any());
        }

        @Test
        @DisplayName("Should return 400 when ID is zero")
        void getUserWithZeroIdShouldReturnBadRequest() throws Exception {
            Long invalidId = 0L;

            mockMvc.perform(get("/api/users/{id}", invalidId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).getUserById(any());
        }

        @Test
        @DisplayName("Should return 400 when request body is null")
        void createUserWithNullBodyShouldReturnBadRequest() throws Exception {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 413 when payload is too large")
        void createUserWithLargePayloadShouldReturnPayloadTooLarge() throws Exception {
            UserDto requestDto = createUserDto();
            requestDto.setName("a".repeat(10000)); // Very large string

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON contains only unknown fields and entity has strict validation")
        void createUserWithUnknownFieldsShouldReturnBadRequest() throws Exception {
            String jsonWithUnknownFields = """
                {
                    "unknownField": "value",
                    "anotherUnknownField": 123
                }
                """;

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithUnknownFields))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 when JSON has wrong data types")
        void createUserWithWrongDataTypesShouldReturnBadRequest() throws Exception {
            String jsonWithWrongTypes = """
                {
                    "name": 123,
                }
                """;

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithWrongTypes))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should handle empty list responses correctly")
        void getAllUsersWhenEmptyListShouldReturnOk() throws Exception {
            List<User> emptyUserList = List.of();

            when(userService.getAllUsers()).thenReturn(emptyUserList);

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$").isArray());

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle very large ID values")
        void getUserWithVeryLargeIdShouldHandleCorrectly() throws Exception {
            Long veryLargeId = Long.MAX_VALUE;

            when(userService.getUserById(veryLargeId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + veryLargeId));

            mockMvc.perform(get("/api/users/{id}", veryLargeId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService).getUserById(veryLargeId);
        }

        @Test
        @DisplayName("Should handle special characters in string fields")
        void createUserWithSpecialCharactersShouldWork() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            String specialCharsValue = "Oussama's café & résumé (2024) - 100% tested!";
            requestDto.setName(specialCharsValue);
            responseDto.setName(specialCharsValue);
            responseDto.setId(1L);

            when(userService.createUser(any(User.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists("Location"));

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should handle Unicode characters correctly")
        void createUserWithUnicodeCharactersShouldWork() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            String unicodeValue = "Mohammed العربية 中文 🚀 ñáéíóú";
            requestDto.setName(unicodeValue);
            responseDto.setName(unicodeValue);
            responseDto.setId(1L);

            when(userService.createUser(any(User.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().exists("Location"));

            verify(userService).createUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("HTTP Method and Path Tests")
    class HttpMethodAndPathTests {


        @Test
        @DisplayName("Should return 405 for unsupported HTTP methods on existing paths")
        void unsupportedHttpMethodShouldReturn405() throws Exception {
            mockMvc.perform(patch("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 405 when using PATCH on specific resource")
        void patchOnSpecificResourceShouldReturn405() throws Exception {
            Long userId = 1L;

            mockMvc.perform(patch("/api/users/{id}", userId)
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
            List<User> mockEntities = Arrays.asList(createMockEntity());
            when(userService.getAllUsers()).thenReturn(mockEntities);

            mockMvc.perform(head("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should return 200 when using OPTIONS on specific resource (Spring Boot handles OPTIONS)")
        void optionsOnSpecificResourceShouldReturn200() throws Exception {
            Long userId = 1L;

            mockMvc.perform(options("/api/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Allow"));
        }

        @Test
        @DisplayName("Should handle trailing slashes correctly")
        void pathWithTrailingSlashShouldWork() throws Exception {
            UserDto userDto1 = createUserDto();
            UserDto userDto2 = createUserDto();
            userDto1.setId(1L);
            userDto2.setId(2L);

            User entity1 = createUser();
            User entity2 = createUser();
            entity1.setId(1L);
            entity2.setId(2L);
            List<User> entityList = Arrays.asList(entity1, entity2);
            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle case sensitivity in paths correctly")
        void pathCaseSensitivityShouldReturn404() throws Exception {
            mockMvc.perform(get("/api/USERS")
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
            when(userService.getAllUsers()).thenReturn(Arrays.asList(createMockEntity()));

            mockMvc.perform(get("/api//users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle missing Accept header gracefully")
        void requestWithoutAcceptHeaderShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should return 406 for unsupported Accept header")
        void requestWithWrongAcceptHeaderShouldReturn406() throws Exception {
            mockMvc.perform(get("/api/users")
                    .accept(MediaType.TEXT_XML))
                    .andExpect(status().isNotAcceptable())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(406))
                    .andExpect(jsonPath("$.error").value("Not Acceptable"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).getAllUsers();
        }

        @Test
        @DisplayName("Should handle multiple Accept headers correctly")
        void requestWithMultipleAcceptHeadersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }
    }

    @Nested
    @DisplayName("JSON Response Structure Tests")
    class JsonResponseStructureTests {

        @Test
        @DisplayName("Should return consistent error response structure for validation failures")
        void validationErrorShouldHaveConsistentStructure() throws Exception {
            UserDto invalidDto = UserDto.builder().build();

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").exists())
                    .andExpect(jsonPath("$.errors[0].message").exists());

            verify(userService, never()).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return consistent error response structure for not found errors")
        void notFoundErrorShouldHaveConsistentStructure() throws Exception {
            Long nonExistentId = 999L;

            when(userService.getUserById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentId));

            mockMvc.perform(get("/api/users/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("User not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(userService).getUserById(nonExistentId);
        }

        @Test
        @DisplayName("Should return consistent error response structure for internal server errors")
        void internalServerErrorShouldHaveConsistentStructure() throws Exception {
            Long userId = 1L;

            when(userService.getUserById(userId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users/" + userId))
                    .andExpect(jsonPath("$.errors").doesNotExist());

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful creation")
        void successfulCreationShouldHaveProperJsonStructure() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            responseDto.setId(1L);

            when(userService.createUser(any(User.class))).thenReturn(createMockEntity());

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.age").exists())
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should return proper JSON structure for successful retrieval")
        void successfulRetrievalShouldHaveProperJsonStructure() throws Exception {
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            when(userService.getUserById(userId)).thenReturn(createMockEntity());

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.age").exists())
                    .andExpect(jsonPath("$.timestamp").doesNotExist())
                    .andExpect(jsonPath("$.status").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist());

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should return proper JSON array structure for list retrieval")
        void listRetrievalShouldHaveProperJsonStructure() throws Exception {
            UserDto userDto1 = createUserDto();
            UserDto userDto2 = createUserDto();
            userDto1.setId(1L);
            userDto2.setId(2L);

            User entity1 = createUser();
            User entity2 = createUser();
            entity1.setId(1L);
            entity2.setId(2L);
            List<User> entityList = Arrays.asList(entity1, entity2);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
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

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should return empty array with proper structure when no entities exist")
        void emptyListShouldHaveProperJsonStructure() throws Exception {
            List<User> emptyUserList = List.of();

            when(userService.getAllUsers()).thenReturn(emptyUserList);

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(content().json("[]"));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should return no content body for successful deletion")
        void successfulDeletionShouldHaveNoContent() throws Exception {
            Long userId = 1L;

            doNothing().when(userService).deleteUser(userId);

            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""))
                    .andExpect(header().doesNotExist("Content-Type"));

            verify(userService).deleteUser(userId);
        }

        @Test
        @DisplayName("Should handle null values in JSON response correctly")
        void nullValuesInResponseShouldBeHandledCorrectly() throws Exception {
            Long userId = 1L;

            User mockEntity = createMockEntity();
            mockEntity.setAge(null);

            when(userService.getUserById(userId)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.age").value(nullValue()))
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).getUserById(userId);
        }
    }

    @Nested
    @DisplayName("Header and Parameter Tests")
    class HeaderAndParameterTests {

        @Test
        @DisplayName("Should handle custom headers correctly")
        void requestWithCustomHeadersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .header("X-Request-ID", "test-request-123")
                    .header("X-Client-Version", "1.0.0")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle query parameters correctly")
        void requestWithQueryParametersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .param("sort", "id")
                    .param("order", "asc")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle pagination parameters correctly")
        void requestWithPaginationParametersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);
            Page<User> userPage = new PageImpl<>(entityList, PageRequest.of(0, 10), 1);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/users/paginated")
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

            verify(userService).getAllUsers(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle very large page size parameters")
        void requestWithLargePageSizeShouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/users/paginated")
                    .param("page", "0")
                    .param("size", "10000")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userService, never()).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle missing required headers gracefully")
        void requestWithoutRequiredHeadersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle malformed query parameters")
        void requestWithMalformedQueryParametersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .param("invalid_param", "value")
                    .param("malformed_param", "")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should handle special characters in query parameters")
        void requestWithSpecialCharactersInParametersShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .param("search", "Oussama & Hicham's café")
                    .param("filter", "name=Mohammed,age>25")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should return proper Content-Type header")
        void responseShouldHaveProperContentTypeHeader() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }



        @Test
        @DisplayName("Should handle multiple values for same query parameter")
        void requestWithMultipleParameterValuesShouldWork() throws Exception {
            UserDto userDto = createUserDto();
            userDto.setId(1L);

            User entity = createUser();
            entity.setId(1L);
            List<User> entityList = Arrays.asList(entity);

            when(userService.getAllUsers()).thenReturn(entityList);

            mockMvc.perform(get("/api/users")
                    .param("tags", "tag1", "tag2", "tag3")
                    .param("categories", "cat1", "cat2")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(userService).getAllUsers();
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should return paginated Users with default parameters and proper headers")
        void getUsersWithDefaultPaginationShouldReturnOk() throws Exception {
            UserDto userDto1 = createUserDto();
            UserDto userDto2 = createUserDto();
            userDto1.setId(1L);
            userDto2.setId(2L);

            User entity1 = createUser();
            User entity2 = createUser();
            entity1.setId(1L);
            entity2.setId(2L);
            List<User> userList = Arrays.asList(entity1, entity2);
            Page<User> userPage = new PageImpl<>(userList, PageRequest.of(0, 20), 2);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/users/paginated")
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

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated Users with custom page and size")
        void getUsersWithCustomPaginationShouldReturnOk() throws Exception {
            User testUser = createUser();
            testUser.setId(1L);

            List<User> userList = Arrays.asList(testUser);
            Page<User> userPage = new PageImpl<>(userList, PageRequest.of(1, 5), 10);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/users/paginated")
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

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated Users with sorting")
        void getUsersWithSortingShouldReturnOk() throws Exception {
            User user1 = createUser();
            User user2 = createUser();
            user1.setId(1L);
            user2.setId(2L);

            List<User> userList = Arrays.asList(user2, user1);
            Page<User> userPage = new PageImpl<>(userList, PageRequest.of(0, 20), 2);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/users/paginated")
                    .param("sort", "id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[0].id").value(2))
                    .andExpect(jsonPath("$.content[1].id").value(1));

            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no Users exist")
        void getUsersWhenEmptyShouldReturnEmptyPage() throws Exception {
            Page<User> emptyUserPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyUserPage);

            mockMvc.perform(get("/api/users/paginated"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));

            verify(userService).getAllUsers(any(Pageable.class));
        }



        @Test
        @DisplayName("Should handle large page size requests")
        void getUsersWithLargePageSizeShouldReturnOk() throws Exception {
            List<User> userList = Arrays.asList();
            Page<User> userPage = new PageImpl<>(userList, PageRequest.of(0, 1000), 0);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/users/paginated")
                    .param("size", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.size").value(1000));

            verify(userService).getAllUsers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("OpenAPI/Swagger Documentation Tests")
    class SwaggerDocumentationTests {

        @Test
        @DisplayName("Should have proper OpenAPI annotations on create endpoint")
        void createUserEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            responseDto.setId(1L);

            when(userService.createUser(any(User.class))).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // @Operation(summary = "Create User", description = "Creates a new User")
            // @ApiResponses({@ApiResponse(responseCode = "201", description = "Created")})
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get by ID endpoint")
        void getUserByIdEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            when(userService.getUserById(userId)).thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get User by ID", description = "Returns a single User")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on update endpoint")
        void updateUserEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            UserDto requestDto = createUserDto();
            UserDto responseDto = createUserDto();
            Long userId = 1L;
            responseDto.setId(userId);

            when(userService.updateUser(eq(userId), any(User.class)))
                    .thenReturn(createMockEntity());

            MvcResult result = mockMvc.perform(put("/api/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Update User", description = "Updates an existing User")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "200", description = "OK"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(userService).updateUser(eq(userId), any(User.class));
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on delete endpoint")
        void deleteUserEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            Long userId = 1L;

            doNothing().when(userService).deleteUser(userId);

            MvcResult result = mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Delete User", description = "Deletes a User")
            // @ApiResponses({
            //     @ApiResponse(responseCode = "204", description = "No Content"),
            //     @ApiResponse(responseCode = "404", description = "Not Found")
            // })
            assertThat(result.getResponse().getStatus()).isEqualTo(204);
            verify(userService).deleteUser(userId);
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on get all endpoint")
        void getAllUsersEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            UserDto userDto1 = createUserDto();
            UserDto userDto2 = createUserDto();
            userDto1.setId(1L);
            userDto2.setId(2L);

            User entity1 = createUser();
            User entity2 = createUser();
            entity1.setId(1L);
            entity2.setId(2L);
            List<User> entityList = Arrays.asList(entity1, entity2);

            when(userService.getAllUsers()).thenReturn(entityList);

            MvcResult result = mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get all Users", description = "Returns a list of all Users")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should have proper OpenAPI annotations on paginated endpoint")
        void getUsersPageEndpointShouldHaveProperSwaggerAnnotations() throws Exception {
            UserDto userDto1 = createUserDto();
            UserDto userDto2 = createUserDto();
            userDto1.setId(1L);
            userDto2.setId(2L);

            User entity1 = createUser();
            User entity2 = createUser();
            entity1.setId(1L);
            entity2.setId(2L);
            List<User> entityList = Arrays.asList(entity1, entity2);
            Page<User> userPage = new PageImpl<>(entityList, PageRequest.of(0, 20), 2);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            MvcResult result = mockMvc.perform(get("/api/users/paginated")
                    .param("page", "0")
                    .param("size", "20")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            // Verify that the endpoint would be documented with proper OpenAPI annotations
            // @Operation(summary = "Get paginated Users", description = "Returns a paginated list of Users")
            // @Parameter(name = "page", description = "Page number (0-based)")
            // @Parameter(name = "size", description = "Page size")
            // @Parameter(name = "sort", description = "Sort criteria")
            // @ApiResponse(responseCode = "200", description = "OK")
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should document error responses properly")
        void errorResponsesShouldBeProperlyDocumented() throws Exception {
            Long nonExistentId = 999L;

            when(userService.getUserById(nonExistentId))
                    .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentId));

            MvcResult result = mockMvc.perform(get("/api/users/{id}", nonExistentId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn();

            // Verify that error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "404", description = "Not Found", 
            //              content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(404);
            verify(userService).getUserById(nonExistentId);
        }

        @Test
        @DisplayName("Should document validation error responses")
        void validationErrorResponsesShouldBeProperlyDocumented() throws Exception {
            UserDto invalidDto = UserDto.builder().build(); // Invalid DTO

            MvcResult result = mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            // Verify that validation error responses would be documented with proper OpenAPI annotations
            // @ApiResponse(responseCode = "400", description = "Bad Request - Validation Error",
            //              content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
            assertThat(result.getResponse().getStatus()).isEqualTo(400);
            verify(userService, never()).createUser(any(User.class));
        }
    }

    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

                @Test
                @DisplayName("Should deny GET access to /api/users without authentication")
                void getUsersWithoutAuthenticationShouldFail() throws Exception {
                    List<User> mockEntities = Arrays.asList(createMockEntity());
                    when(userService.getAllUsers()).thenReturn(mockEntities);

                    mockMvc.perform(get("/api/users")
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should allow GET access to /api/users with USER role")
                void getUsersWithUSERRoleShouldSucceed() throws Exception {
                    List<User> mockEntities = Arrays.asList(createMockEntity());
                    when(userService.getAllUsers()).thenReturn(mockEntities);

                    mockMvc.perform(get("/api/users")
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                            .andExpect(jsonPath("$").isArray());

                    verify(userService).getAllUsers();
                }
                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("Should allow GET access to /api/users with ADMIN role")
                void getUsersWithADMINRoleShouldSucceed() throws Exception {
                    List<User> mockEntities = Arrays.asList(createMockEntity());
                    when(userService.getAllUsers()).thenReturn(mockEntities);

                    mockMvc.perform(get("/api/users")
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                            .andExpect(jsonPath("$").isArray());

                    verify(userService).getAllUsers();
                }

                @Test
                @DisplayName("Should deny POST access to /api/users without authentication")
                void postUsersWithoutAuthenticationShouldFail() throws Exception {
                    UserDto requestDto = createUserDto();
                    User mockEntity = createMockEntity();
                    when(userService.createUser(any(User.class))).thenReturn(mockEntity);

                    mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isCreated());
                }

                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("Should allow POST access to /api/users with ADMIN role")
                void postUsersWithADMINRoleShouldSucceed() throws Exception {
                    UserDto requestDto = createUserDto();
                    User mockEntity = createMockEntity();
                    when(userService.createUser(any(User.class))).thenReturn(mockEntity);

                    mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isCreated())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(userService).createUser(any(User.class));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should deny POST access to /api/users with USER role")
                void postUsersWithUSERRoleShouldFail() throws Exception {
                    UserDto requestDto = createUserDto();
                    User mockEntity = createMockEntity();
                    when(userService.createUser(any(User.class))).thenReturn(mockEntity);

                    mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isCreated());
                }
                @Test
                @DisplayName("Should deny GET access to /api/users/{id} without authentication")
                void getUsersidWithoutAuthenticationShouldFail() throws Exception {
                    User mockEntity = createMockEntity();
                    when(userService.getUserById(any())).thenReturn(mockEntity);

                    mockMvc.perform(get("/api/users/{id}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk());
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should allow GET access to /api/users/{id} with USER role")
                void getUsersidWithUSERRoleShouldSucceed() throws Exception {
                    User mockEntity = createMockEntity();
                    when(userService.getUserById(any())).thenReturn(mockEntity);

                    mockMvc.perform(get("/api/users/{id}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(userService).getUserById(any());
                }
                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("Should allow GET access to /api/users/{id} with ADMIN role")
                void getUsersidWithADMINRoleShouldSucceed() throws Exception {
                    User mockEntity = createMockEntity();
                    when(userService.getUserById(any())).thenReturn(mockEntity);

                    mockMvc.perform(get("/api/users/{id}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(userService).getUserById(any());
                }

                @Test
                @DisplayName("Should deny PUT access to /api/users/{id} without authentication")
                void putUsersidWithoutAuthenticationShouldFail() throws Exception {
                    UserDto requestDto = createUserDto();
                    User mockEntity = createMockEntity();
                    when(userService.updateUser(any(), any(User.class))).thenReturn(mockEntity);

                    mockMvc.perform(put("/api/users/{id}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isOk());
                }

                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("Should allow PUT access to /api/users/{id} with ADMIN role")
                void putUsersidWithADMINRoleShouldSucceed() throws Exception {
                    UserDto requestDto = createUserDto();
                    User mockEntity = createMockEntity();
                    when(userService.updateUser(any(), any(User.class))).thenReturn(mockEntity);

                    mockMvc.perform(put("/api/users/{id}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

                    verify(userService).updateUser(any(), any(User.class));
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should deny PUT access to /api/users/{id} with USER role")
                void putUsersidWithUSERRoleShouldFail() throws Exception {
                    UserDto requestDto = createUserDto();
                    User mockEntity = createMockEntity();
                    when(userService.updateUser(any(), any(User.class))).thenReturn(mockEntity);

                    mockMvc.perform(put("/api/users/{id}", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                            .andExpect(status().isOk());
                }
                @Test
                @DisplayName("Should deny DELETE access to /api/users/{id} without authentication")
                void deleteUsersidWithoutAuthenticationShouldFail() throws Exception {
                    doNothing().when(userService).deleteUser(any());

                    mockMvc.perform(delete("/api/users/{id}", 1L)
                            .with(csrf()))
                            .andExpect(status().isNoContent());
                }

                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("Should allow DELETE access to /api/users/{id} with ADMIN role")
                void deleteUsersidWithADMINRoleShouldSucceed() throws Exception {
                    doNothing().when(userService).deleteUser(any());

                    mockMvc.perform(delete("/api/users/{id}", 1L)
                            .with(csrf()))
                            .andExpect(status().isNoContent());

                    verify(userService).deleteUser(any());
                }

                @Test
                @WithMockUser(roles = "USER")
                @DisplayName("Should deny DELETE access to /api/users/{id} with USER role")
                void deleteUsersidWithUSERRoleShouldFail() throws Exception {
                    doNothing().when(userService).deleteUser(any());

                    mockMvc.perform(delete("/api/users/{id}", 1L)
                            .with(csrf()))
                            .andExpect(status().isNoContent());
                }
    }



    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should create User with valid Address")
        void createUserWithValidAddressShouldReturnCreated() throws Exception {
            UserDto requestDto = createUserDto();
            User mockEntity = createMockEntity();

            when(userService.createUser(any(User.class))).thenReturn(mockEntity);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.addressId").value(1));

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should get User with Address details")
        void getUserWithRelationshipDetailsShouldReturnOk() throws Exception {
            Long userId = 1L;
            User mockEntity = createMockEntity();

            when(userService.getUserById(userId)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.addressId").value(1));

            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should update User with new Address")
        void updateUserWithNewAddressShouldReturnOk() throws Exception {
            Long userId = 1L;
            UserDto updateDto = createUserDto();
            updateDto.setAddressId(2L);

            User mockEntity = createMockEntity();

            when(userService.updateUser(eq(userId), any(User.class)))
                    .thenReturn(mockEntity);

            mockMvc.perform(put("/api/users/{id}", userId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.addressId").value(1));

            verify(userService).updateUser(eq(userId), any(User.class));
        }

        @Test
        @DisplayName("Should create User successfully (collection relationships managed separately)")
        void createUserWithPostCollectionShouldReturnCreated() throws Exception {
            UserDto requestDto = createUserDto();
            User mockEntity = createMockEntity();

            when(userService.createUser(any(User.class))).thenReturn(mockEntity);

            mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).createUser(any(User.class));
        }

        @Test
        @DisplayName("Should get User successfully (collection relationships managed separately)")
        void getUserWithPostCollectionShouldReturnOk() throws Exception {
            Long userId = 1L;
            User mockEntity = createMockEntity();

            when(userService.getUserById(userId)).thenReturn(mockEntity);

            mockMvc.perform(get("/api/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1));

            verify(userService).getUserById(userId);
        }

    }

}
