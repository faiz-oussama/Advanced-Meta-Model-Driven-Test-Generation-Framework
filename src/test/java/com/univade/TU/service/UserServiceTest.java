package com.univade.TU.service;

import com.univade.TU.testdata.UserTestDataBuilder;

import com.univade.TU.testdata.AddressTestDataBuilder;
import com.univade.TU.testdata.PostTestDataBuilder;

import com.univade.TU.entity.User;

import com.univade.TU.entity.Address;
import com.univade.TU.entity.Post;

import com.univade.TU.repository.UserRepository;
import com.univade.TU.repository.AddressRepository;
import com.univade.TU.repository.PostRepository;

import com.univade.TU.service.UserService;
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
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        reset(userRepository);
    }

    private User createUser() {
        return UserTestDataBuilder.aValidUser().build();
    }

    private Address createAddress() {
        return AddressTestDataBuilder.aDefaultAddress().build();
    }

    private Post createPost() {
        return PostTestDataBuilder.aDefaultPost().build();
    }

    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createUserShouldSaveAndReturnEntity() {
            User testUser = createUser();
            User savedUser = createUser();
            savedUser.setId(1L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        void createUserWithNullShouldThrowException() {
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void getUserByIdShouldReturnEntityWhenExists() {
            Long testId = 1L;
            User testUser = createUser();
            testUser.setId(testId);

            when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

            User result = userService.getUserById(testId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(userRepository).findById(testId);
        }

        @Test
        void getUserByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(userRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(userRepository).findById(testId);
        }

        @Test
        void getAllUsersShouldReturnAllEntities() {
            User user1 = createUser();
            User user2 = createUser();
            List<User> expectedUsers = List.of(user1, user2);

            when(userRepository.findAll()).thenReturn(expectedUsers);

            List<User> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedUsers);
            verify(userRepository).findAll();
        }

        @Test
        void getAllUsersWhenEmptyShouldReturnEmptyList() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            List<User> result = userService.getAllUsers();

            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }

        @Test
        void updateUserShouldUpdateAndReturnEntity() {
            Long testId = 1L;
            User existingUser = createUser();
            existingUser.setId(testId);

            User updatedUser = createUser();
            updatedUser.setId(testId);
            updatedUser.setName("Updated Name");

            when(userRepository.findById(testId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            User result = userService.updateUser(testId, updatedUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(userRepository).findById(testId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        void updateUserWithNonExistentIdShouldThrowException() {
            Long testId = 999L;
            User updatedUser = createUser();

            when(userRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(testId, updatedUser))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(userRepository).findById(testId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void deleteUserByIdShouldDeleteWhenExists() {
            Long testId = 1L;

            when(userRepository.existsById(testId)).thenReturn(true);
            doNothing().when(userRepository).deleteById(testId);

            assertThatCode(() -> userService.deleteUser(testId))
                    .doesNotThrowAnyException();

            verify(userRepository).existsById(testId);
            verify(userRepository).deleteById(testId);
        }

        @Test
        void deleteUserByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(userRepository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(userRepository).existsById(testId);
            verify(userRepository, never()).deleteById(testId);
        }

        @Test
        void existsUserByIdShouldReturnTrueWhenExists() {
            Long testId = 1L;

            when(userRepository.existsById(testId)).thenReturn(true);

            boolean result = userService.existsUserById(testId);

            assertThat(result).isTrue();
            verify(userRepository).existsById(testId);
        }

        @Test
        void existsUserByIdShouldReturnFalseWhenNotExists() {
            Long testId = 999L;

            when(userRepository.existsById(testId)).thenReturn(false);

            boolean result = userService.existsUserById(testId);

            assertThat(result).isFalse();
            verify(userRepository).existsById(testId);
        }

        @Test
        void countUsersShouldReturnCorrectCount() {
            long expectedCount = 5L;

            when(userRepository.count()).thenReturn(expectedCount);

            long result = userService.countUsers();

            assertThat(result).isEqualTo(expectedCount);
            verify(userRepository).count();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when creating User with null entity")
        void createUserWithNullShouldThrowException() {
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot be null");

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating User with null entity")
        void updateUserWithNullShouldThrowException() {
            Long testId = 1L;

            assertThatThrownBy(() -> userService.updateUser(testId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot be null");

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating User with null ID")
        void updateUserWithNullIdShouldThrowException() {
            User testUser = createUser();

            assertThatThrownBy(() -> userService.updateUser(null, testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw exception when deleting User with null ID")
        void deleteUserWithNullIdShouldThrowException() {
            assertThatThrownBy(() -> userService.deleteUser(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent User")
        void updateUserWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;
            User testUser = createUser();

            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(nonExistentId, testUser))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: " + nonExistentId);

            verify(userRepository).findById(nonExistentId);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent User")
        void deleteUserWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;

            when(userRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: " + nonExistentId);

            verify(userRepository).existsById(nonExistentId);
            verify(userRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully when finding User")
        void findUserByIdShouldHandleRepositoryException() {
            Long testId = 1L;

            when(userRepository.findById(testId))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> userService.getUserById(testId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(userRepository).findById(testId);
        }

        @Test
        @DisplayName("Should validate business rules before saving User")
        void createUserShouldValidateBusinessRules() {
            User testUser = createUser();
            User savedUser = createUser();
            savedUser.setId(1L);

            when(userRepository.save(testUser)).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedUser);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should check existence before operations")
        void existsUserByIdShouldReturnCorrectValue() {
            Long existingId = 1L;
            Long nonExistentId = 999L;

            when(userRepository.existsById(existingId)).thenReturn(true);
            when(userRepository.existsById(nonExistentId)).thenReturn(false);

            boolean existsResult = userService.existsUserById(existingId);
            boolean notExistsResult = userService.existsUserById(nonExistentId);

            assertThat(existsResult).isTrue();
            assertThat(notExistsResult).isFalse();
            verify(userRepository).existsById(existingId);
            verify(userRepository).existsById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null ID gracefully in exists check")
        void existsUserByIdWithNullShouldReturnFalse() {
            boolean result = userService.existsUserById(null);

            assertThat(result).isFalse();
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should handle validation errors from JPA/Hibernate")
        void createUserShouldHandleConstraintViolationException() {
            User invalidUser = createUser();
            invalidUser.setName(null);

            when(userRepository.save(invalidUser))
                    .thenThrow(new ConstraintViolationException("Validation failed", null));

            assertThatThrownBy(() -> userService.createUser(invalidUser))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Validation failed");

            verify(userRepository).save(invalidUser);
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void createUserShouldHandleDataIntegrityViolationException() {
            User duplicateUser = createUser();
            duplicateUser.setEmail("duplicate@example.com");

            when(userRepository.save(duplicateUser))
                    .thenThrow(new DataIntegrityViolationException("Duplicate constraint violation"));

            assertThatThrownBy(() -> userService.createUser(duplicateUser))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Duplicate constraint violation");

            verify(userRepository).save(duplicateUser);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void getAllUsersWithPageableShouldReturnPagedResults() {
            User user1 = createUser();
            User user2 = createUser();
            User user3 = createUser();

            List<User> userList = List.of(user1, user2);
            Page<User> expectedPage = new PageImpl<>(userList, PageRequest.of(0, 2), 3);

            Pageable pageable = PageRequest.of(0, 2);
            when(userRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
            verify(userRepository).findAll(pageable);
        }

        @Test
        void getAllUsersWithPageableAndSortShouldReturnSortedResults() {
            User user1 = createUser();
            user1.setName("Hicham");
            User user2 = createUser();
            user2.setName("Oussama");

            List<User> sortedUserList = List.of(user1, user2);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<User> expectedPage = new PageImpl<>(sortedUserList, pageable, 2);

            when(userRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Hicham");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Oussama");
            verify(userRepository).findAll(pageable);
        }

        @Test
        void getAllUsersWithEmptyPageShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(userRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            verify(userRepository).findAll(pageable);
        }

        @Test
        void getAllUsersWithLargePageSizeShouldReturnAllResults() {
            User user1 = createUser();
            User user2 = createUser();
            User user3 = createUser();

            List<User> allUsers = List.of(user1, user2, user3);
            Pageable pageable = PageRequest.of(0, 100);
            Page<User> expectedPage = new PageImpl<>(allUsers, pageable, 3);

            when(userRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(userRepository).findAll(pageable);
        }

        @Test
        void getAllUsersWithSecondPageShouldReturnCorrectResults() {
            User user3 = createUser();
            User user4 = createUser();

            List<User> secondPageUsers = List.of(user3, user4);
            Pageable pageable = PageRequest.of(1, 2);
            Page<User> expectedPage = new PageImpl<>(secondPageUsers, pageable, 4);

            when(userRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            verify(userRepository).findAll(pageable);
        }



        @Test
        void getAllUsersWithDescendingSortShouldReturnCorrectOrder() {
            User user1 = createUser();
            user1.setName("Ilyass");
            User user2 = createUser();
            user2.setName("Oussama");

            List<User> sortedUserList = List.of(user2, user1);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
            Page<User> expectedPage = new PageImpl<>(sortedUserList, pageable, 2);

            when(userRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Oussama");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Ilyass");
            verify(userRepository).findAll(pageable);
        }

        @Test
        void getAllUsersWithCustomPageSizeShouldRespectPageSize() {
            User user1 = createUser();
            User user2 = createUser();

            List<User> userList = List.of(user1, user2);
            Pageable pageable = PageRequest.of(0, 1);
            Page<User> expectedPage = new PageImpl<>(List.of(user1), pageable, 2);

            when(userRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<User> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(1);
            verify(userRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Service Validation Tests")
    class ServiceValidationTests {

        @Test
        @DisplayName("Should validate entity before saving")
        void createUserShouldValidateEntity() {
            User testUser = createUser();
            User savedUser = createUser();
            savedUser.setId(1L);

            when(userRepository.save(testUser)).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedUser);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should handle repository exceptions during validation")
        void createUserShouldHandleRepositoryExceptions() {
            User testUser = createUser();

            when(userRepository.save(testUser))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> userService.createUser(testUser))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should validate entity state before update")
        void updateUserShouldValidateEntityState() {
            Long testId = 1L;
            User existingUser = createUser();
            existingUser.setId(testId);

            User updatedUser = createUser();
            User savedUser = createUser();
            savedUser.setId(testId);

            when(userRepository.findById(testId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.updateUser(testId, updatedUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(userRepository).findById(testId);
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        void createUserWithOptionalAddressShouldSucceed() {
            User testUser = createUser();
            Address testAddress = createAddress();
            testAddress.setId(1L);
            testUser.setAddress(testAddress);

            User savedUser = createUser();
            savedUser.setAddress(testAddress);
            savedUser.setId(1L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getAddress()).isNotNull();
            verify(userRepository).save(testUser);
        }

        @Test
        void createUserWithoutOptionalAddressShouldSucceed() {
            User testUser = createUser();
            testUser.setAddress(null);

            User savedUser = createUser();
            savedUser.setAddress(null);
            savedUser.setId(1L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getAddress()).isNull();
            verify(userRepository).save(testUser);
        }

        @Test
        void createUserWithMultiplePostsShouldSucceed() {
            User testUser = createUser();
            Post post1 = createPost();
            Post post2 = createPost();
            post1.setId(1L);
            post2.setId(2L);

            List<Post> postsList = List.of(post1, post2);
            testUser.setPosts(postsList);

            User savedUser = createUser();
            savedUser.setPosts(postsList);
            savedUser.setId(1L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).hasSize(2);
            verify(userRepository).save(testUser);
        }

        @Test
        void createUserWithEmptyPostsShouldSucceed() {
            User testUser = createUser();
            testUser.setPosts(Collections.emptyList());

            User savedUser = createUser();
            savedUser.setPosts(Collections.emptyList());
            savedUser.setId(1L);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userService.createUser(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isEmpty();
            verify(userRepository).save(testUser);
        }

    }

    @Nested
    @DisplayName("Relationship Update Tests")
    class RelationshipUpdateTests {

        @Test
        @DisplayName("Should update User with new Address")
        void updateUserWithNewAddressShouldWork() {
            Long userId = 1L;
            User existingUser = createUser();
            existingUser.setId(userId);

            Address newAddress = createAddress();
            User updatedUser = createUser();
            updatedUser.setId(userId);
            updatedUser.setAddress(newAddress);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            User result = userService.updateUser(userId, updatedUser);

            assertThat(result).isNotNull();
            assertThat(result.getAddress()).isEqualTo(newAddress);
            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle null Address update")
        void updateUserWithNullAddressShouldWork() {
            Long userId = 1L;
            User existingUser = createUser();
            existingUser.setId(userId);
            existingUser.setAddress(createAddress());

            User updatedUser = createUser();
            updatedUser.setId(userId);
            updatedUser.setAddress(null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            User result = userService.updateUser(userId, updatedUser);

            assertThat(result).isNotNull();
            assertThat(result.getAddress()).isNull();
            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should update User with new posts")
        void updateUserWithNewPostsShouldWork() {
            Long userId = 1L;
            User existingUser = createUser();
            existingUser.setId(userId);

            Post newPost = createPost();
            User updatedUser = createUser();
            updatedUser.setId(userId);
            updatedUser.getPosts().add(newPost);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            User result = userService.updateUser(userId, updatedUser);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).hasSize(1);
            assertThat(result.getPosts().get(0)).isEqualTo(newPost);
            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle clearing posts collection")
        void updateUserWithClearedPostsShouldWork() {
            Long userId = 1L;
            User existingUser = createUser();
            existingUser.setId(userId);
            existingUser.getPosts().add(createPost());

            User updatedUser = createUser();
            updatedUser.setId(userId);
            updatedUser.getPosts().clear();

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(updatedUser);

            User result = userService.updateUser(userId, updatedUser);

            assertThat(result).isNotNull();
            assertThat(result.getPosts()).isEmpty();
            verify(userRepository).findById(userId);
            verify(userRepository).save(any(User.class));
        }

    }

    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private UserService userServiceIntegration;

        @Autowired
        private UserRepository userRepositoryIntegration;

        @Autowired
        private jakarta.persistence.EntityManager entityManager;

        @Autowired
        private AddressRepository addressRepositoryIntegration;

        @Autowired
        private PostRepository postRepositoryIntegration;

        private User testUserIntegration;

        @BeforeEach
        void setUpIntegration() {
            testUserIntegration = createTestEntity();
        }

        private User createTestEntity() {
            User entity = UserTestDataBuilder.aDefaultUser().build();
            return entity;
        }

        @Nested
        @DisplayName("Business Logic Integration Tests")
        class BusinessLogicIntegrationTests {

            @Test
            @DisplayName("Should create User and persist to database")
            void shouldCreateUserAndPersistToDatabase() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);

                assertThat(created).isNotNull();
                assertThat(created.getId()).isNotNull();
                
                Optional<User> persisted = userRepositoryIntegration.findById(created.getId());
                assertThat(persisted).isPresent();
                assertThat(persisted.get().getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should update User and persist changes")
            void shouldUpdateUserAndPersistChanges() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);
                
                created.setName("Updated name");

                User updated = userServiceIntegration.updateUser(created.getId(), created);

                assertThat(updated).isNotNull();
                User persisted = userRepositoryIntegration.findById(created.getId()).orElseThrow();
                assertThat(persisted.getName()).isEqualTo("Updated name");
            }

            @Test
            @DisplayName("Should delete User from database")
            void shouldDeleteUserFromDatabase() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);
                Long id = created.getId();

                userServiceIntegration.deleteUser(id);

                Optional<User> deleted = userRepositoryIntegration.findById(id);
                assertThat(deleted).isEmpty();
            }
        }

        @Nested
        @DisplayName("Validation Constraint Integration Tests")
        class ValidationConstraintIntegrationTests {

            @Test
            @DisplayName("Should handle validation errors from JPA/Hibernate")
            void shouldHandleJpaValidationErrors() {
                User invalidUser = UserTestDataBuilder.aDefaultUser()
                        .withName(null)
                        .build();

                try {
                    assertThatThrownBy(() -> {
                        userServiceIntegration.createUser(invalidUser);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } finally {
                    entityManager.clear();
                }
            }

            @Test
            @DisplayName("Should fail when duplicate email is saved")
            void shouldFailWhenDuplicateEmailIsSaved() {
                User firstEntity = UserTestDataBuilder.aDefaultUser()
                        .withEmail("test" + System.currentTimeMillis() + "@example.com")
                        .build();
                User first = userServiceIntegration.createUser(firstEntity);

                entityManager.flush();
                entityManager.clear();

                User duplicate = UserTestDataBuilder.aDefaultUser().build();
                duplicate.setEmail(first.getEmail());

                assertThatThrownBy(() -> {
                    userServiceIntegration.createUser(duplicate);
                    entityManager.flush();
                }).satisfiesAnyOf(
                    throwable -> assertThat(throwable).isInstanceOf(DataIntegrityViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(jakarta.persistence.EntityExistsException.class)
                );
            }
        }

        @Nested
        @DisplayName("Transactional Behavior Integration Tests")
        class TransactionalBehaviorIntegrationTests {

            @Test
            @DisplayName("Should rollback transaction when error occurs during save")
            void shouldRollbackTransactionWhenErrorOccursDuringSave() {
                long initialCount = userRepositoryIntegration.count();

                User invalidUser = UserTestDataBuilder.aDefaultUser()
                        .withName(null)
                        .build();

                try {
                    assertThatThrownBy(() -> {
                        userServiceIntegration.createUser(invalidUser);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } catch (Exception e) {
                    // Clear entity manager after constraint violation to avoid inconsistent state
                    entityManager.clear();
                    throw e;
                }

                // Clear entity manager to avoid stale state
                entityManager.clear();
                long finalCount = userRepositoryIntegration.count();
                assertThat(finalCount).isEqualTo(initialCount);
            }
        }

        @Nested
        @DisplayName("Entity Relationship Integration Tests")
        class EntityRelationshipIntegrationTests {

            @Test
            @DisplayName("Should save User with Address reference")
            void shouldSaveUserWithAddressReference() {
                User testEntity = createTestEntity();
                Address related = AddressTestDataBuilder.aDefaultAddress().build();
                Address savedRelated = addressRepositoryIntegration.save(related);

                testEntity.setAddress(savedRelated);

                User saved = userServiceIntegration.createUser(testEntity);

                assertThat(saved.getAddress()).isNotNull();
                assertThat(saved.getAddress().getId())
                        .isEqualTo(savedRelated.getId());
            }

            @Test
            @DisplayName("Should update User with new Address relationship")
            void shouldUpdateUserWithNewAddress() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);

                Address newAddress = AddressTestDataBuilder.aDefaultAddress().build();
                Address savedAddress = addressRepositoryIntegration.save(newAddress);

                created.setAddress(savedAddress);
                User updated = userServiceIntegration.updateUser(created.getId(), created);

                assertThat(updated.getAddress()).isNotNull();
                assertThat(updated.getAddress().getId()).isEqualTo(savedAddress.getId());
            }

            @Test
            @DisplayName("Should cascade save Post when saving User")
            void shouldCascadeSavePostWhenSavingUser() {
                User testEntity = createTestEntity();
                Post related = PostTestDataBuilder.aDefaultPost().build();
                // Set the relationship properly for bidirectional mapping
                testEntity.getPosts().add(related);
                related.setAuthor(testEntity);

                User saved = userServiceIntegration.createUser(testEntity);

                assertThat(saved.getPosts()).hasSize(1);
                assertThat(saved.getPosts().get(0).getId()).isNotNull();

                Optional<Post> persistedRelated = postRepositoryIntegration
                        .findById(saved.getPosts().get(0).getId());
                assertThat(persistedRelated).isPresent();
            }

            @Test
            @DisplayName("Should remove orphaned Post when removing from User")
            void shouldRemoveOrphanedPostWhenRemovingFromUser() {
                User testEntity = createTestEntity();
                Post related = PostTestDataBuilder.aDefaultPost().build();
                testEntity.getPosts().add(related);
                related.setAuthor(testEntity);
                User saved = userServiceIntegration.createUser(testEntity);

                Long relatedId = saved.getPosts().get(0).getId();
                saved.getPosts().clear();

                userServiceIntegration.updateUser(saved.getId(), saved);

                // Flush to ensure orphan removal is processed
                entityManager.flush();

                Optional<Post> orphaned = postRepositoryIntegration.findById(relatedId);
                assertThat(orphaned).isEmpty();
            }

        }


        @Nested
        @DisplayName("Finder Method Integration Tests")
        class FinderMethodIntegrationTests {

            @Test
            @DisplayName("Should find User by ID correctly")
            void shouldFindUserByIdCorrectly() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);

                User found = userServiceIntegration.getUserById(created.getId());

                assertThat(found).isNotNull();
                assertThat(found.getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should return empty when User not found by ID")
            void shouldReturnEmptyWhenUserNotFoundById() {
                Long nonExistentId = 999L;

                assertThatThrownBy(() -> userServiceIntegration.getUserById(nonExistentId))
                        .isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("Should check existence correctly")
            void shouldCheckExistenceCorrectly() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);
                Long nonExistentId = 999L;

                boolean exists = userServiceIntegration.existsUserById(created.getId());
                boolean notExists = userServiceIntegration.existsUserById(nonExistentId);

                assertThat(exists).isTrue();
                assertThat(notExists).isFalse();
            }

            @Test
            @DisplayName("Should find all Users from database")
            void shouldFindAllUsersFromDatabase() {
                User testEntity1 = createTestEntity();
                User entity1 = userServiceIntegration.createUser(testEntity1);
                User testEntity2 = createTestEntity();
                User entity2 = userServiceIntegration.createUser(testEntity2);

                List<User> all = userServiceIntegration.getAllUsers();

                assertThat(all).hasSizeGreaterThanOrEqualTo(2);
                assertThat(all).extracting(User::getId)
                        .contains(entity1.getId(), entity2.getId());
            }
        }

        @Nested
        @DisplayName("Deletion Logic Integration Tests")
        class DeletionLogicIntegrationTests {

            @Test
            @DisplayName("Should delete User and verify removal")
            void shouldDeleteUserAndVerifyRemoval() {
                User testEntity = createTestEntity();
                User created = userServiceIntegration.createUser(testEntity);
                Long id = created.getId();

                userServiceIntegration.deleteUser(id);

                boolean exists = userRepositoryIntegration.existsById(id);
                assertThat(exists).isFalse();
            }

            @Test
            @DisplayName("Should cascade delete Address when deleting User")
            void shouldCascadeDeleteAddressWhenDeletingUser() {
                User testEntity = createTestEntity();
                Address related = AddressTestDataBuilder.aDefaultAddress().build();
                testEntity.setAddress(related);
                User saved = userServiceIntegration.createUser(testEntity);

                Long relatedId = saved.getAddress().getId();

                userServiceIntegration.deleteUser(saved.getId());

                boolean relatedExists = addressRepositoryIntegration.existsById(relatedId);
                assertThat(relatedExists).isFalse();
            }

            @Test
            @DisplayName("Should cascade delete Post when deleting User")
            void shouldCascadeDeletePostWhenDeletingUser() {
                User testEntity = createTestEntity();
                Post related = PostTestDataBuilder.aDefaultPost().build();
                testEntity.getPosts().add(related);
                related.setAuthor(testEntity);
                User saved = userServiceIntegration.createUser(testEntity);

                Long relatedId = saved.getPosts().get(0).getId();

                userServiceIntegration.deleteUser(saved.getId());

                boolean relatedExists = postRepositoryIntegration.existsById(relatedId);
                assertThat(relatedExists).isFalse();
            }

        }
    }




}
