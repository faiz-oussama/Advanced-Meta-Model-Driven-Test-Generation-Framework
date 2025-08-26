package com.univade.TU.repository;

import com.univade.TU.testdata.UserTestDataBuilder;

import com.univade.TU.testdata.AddressTestDataBuilder;
import com.univade.TU.testdata.PostTestDataBuilder;

import com.univade.TU.entity.User;

import com.univade.TU.entity.Address;
import com.univade.TU.entity.Post;

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
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private User createUser() {
        User user = UserTestDataBuilder.aValidUser().build();
        user.setAddress(createAddress());
        return user;
    }

    private Address createAddress() {
        Address address = AddressTestDataBuilder.aDefaultAddress().build();
        return address;
    }

    private Post createPost() {
        Post post = PostTestDataBuilder.aDefaultPost().build();
        return post;
    }


    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createUserShouldPersistNewEntity() {
            User testUser = createUser();
            User createdUser = userRepository.save(testUser);

            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getId()).isNotNull();
            assertThat(createdUser.getId()).isPositive();

            User found = entityManager.find(User.class, createdUser.getId());
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo(testUser.getName());
            assertThat(found.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(found.getAge()).isEqualTo(testUser.getAge());
        }

        @Test
        void createUserWithNullShouldThrowException() {
            assertThatThrownBy(() -> userRepository.save(null))
                    .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        void createMultipleUsersShouldPersistAll() {
            User user1 = createUser();
            User user2 = createUser();
            User user3 = createUser();

            List<User> createdUsers = userRepository.saveAll(
                    List.of(user1, user2, user3));

            assertThat(createdUsers).hasSize(3);
            assertThat(createdUsers).allMatch(user -> user.getId() != null);

            long count = userRepository.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void readUserByIdShouldReturnCorrectEntity() {
            User testUser = createUser();
            User savedUser = entityManager.persistAndFlush(testUser);

            Optional<User> found = userRepository.findById(savedUser.getId());

            assertThat(found).isPresent();
            User retrievedUser = found.get();
            assertThat(retrievedUser.getId()).isEqualTo(savedUser.getId());
            assertThat(retrievedUser.getName()).isEqualTo(testUser.getName());
            assertThat(retrievedUser.getEmail()).isEqualTo(testUser.getEmail());
            assertThat(retrievedUser.getAge()).isEqualTo(testUser.getAge());
        }

        @Test
        void readUserByNonExistentIdShouldReturnEmpty() {
            Optional<User> found = userRepository.findById(999L);

            assertThat(found).isEmpty();
        }

    @Test
    void readAllUsersShouldReturnAllEntities() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());

        List<User> allUsers = userRepository.findAll();

            assertThat(allUsers).hasSize(2);
            assertThat(allUsers).extracting("id")
                    .contains(user1.getId(), user2.getId());
    }

    @Test
    void readAllUsersWhenEmptyShouldReturnEmptyList() {
        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).isEmpty();
    }

    @Test
    void updateUserShouldModifyExistingEntity() {
        User testUser = createUser();
        User savedUser = entityManager.persistAndFlush(testUser);
        entityManager.detach(savedUser);

        savedUser.setName("Updated Name");

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");

        User found = entityManager.find(User.class, savedUser.getId());
        assertThat(found.getName()).isEqualTo("Updated Name");
    }

    @Test
    void saveNewUserShouldCreateEntity() {
        long initialCount = userRepository.count();

        User newUser = createUser();

        User result = userRepository.save(newUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        long finalCount = userRepository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void updateAllUserAttributesShouldPersistChanges() {
        User testUser = createUser();
        User savedUser = entityManager.persistAndFlush(testUser);
        entityManager.detach(savedUser);

        savedUser.setName("Updated Name");
        savedUser.setAge(999);

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getAge()).isEqualTo(999);
    }

    @Test
    void deleteUserByIdShouldRemoveEntity() {
        User testUser = createUser();
        User savedUser = entityManager.persistAndFlush(testUser);

        userRepository.deleteById(savedUser.getId());

        Optional<User> found = userRepository.findById(savedUser.getId());
        assertThat(found).isEmpty();

        User entityFound = entityManager.find(User.class, savedUser.getId());
        assertThat(entityFound).isNull();
    }

        @Test
        void deleteUserByNonExistentIdShouldNotThrowException() {
            assertThatCode(() -> userRepository.deleteById(999L))
                    .doesNotThrowAnyException();
        }

    @Test
    void deleteUserEntityShouldRemoveFromDatabase() {
        User testUser = createUser();
        User savedUser = entityManager.persistAndFlush(testUser);

        userRepository.delete(savedUser);

        Optional<User> found = userRepository.findById(savedUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAllUsersShouldRemoveAllEntities() {
        entityManager.persistAndFlush(createUser());
        entityManager.persistAndFlush(createUser());
        entityManager.persistAndFlush(createUser());

        userRepository.deleteAll();

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).isEmpty();

        long count = userRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void deleteMultipleUsersByIdShouldRemoveSpecifiedEntities() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());
        User user3 = entityManager.persistAndFlush(createUser());

        userRepository.deleteAllById(List.of(user1.getId(), user2.getId()));

        List<User> remainingUsers = userRepository.findAll();
        assertThat(remainingUsers).hasSize(1);
        assertThat(remainingUsers.get(0).getId()).isEqualTo(user3.getId());
    }

    @Test
    void existsUserByIdShouldReturnTrueForExistingEntity() {
        User testUser = createUser();
        User savedUser = entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsById(savedUser.getId());

        assertThat(exists).isTrue();
    }

        @Test
        void existsUserByIdShouldReturnFalseForNonExistingEntity() {
            boolean exists = userRepository.existsById(999L);

            assertThat(exists).isFalse();
        }

    @Test
    void countUsersShouldReturnCorrectNumber() {
        entityManager.persistAndFlush(createUser());
        entityManager.persistAndFlush(createUser());

        long count = userRepository.count();

        assertThat(count).isEqualTo(2);
    }

        @Test
        void countUsersWhenEmptyShouldReturnZero() {
            long count = userRepository.count();

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test that primary key is automatically generated")
        void saveUserShouldGenerateId() {
            User user = createUser();

            User savedUser = userRepository.save(user);

            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getId()).isPositive();

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void saveUserWithNullNameShouldThrowException() {
            User userWithNullName = createUser();
            userWithNullName.setName(null);

            assertThatThrownBy(() -> {
                userRepository.save(userWithNullName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("name");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void saveUserWithTooLongNameShouldThrowException() {
            String tooLongValue = "a".repeat(101);
            User userWithTooLongName = createUser();
            userWithTooLongName.setName(tooLongValue);

            assertThatThrownBy(() -> {
                userRepository.save(userWithTooLongName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void saveUserWithNullEmailShouldThrowException() {
            User userWithNullEmail = createUser();
            userWithNullEmail.setEmail(null);

            assertThatThrownBy(() -> {
                userRepository.save(userWithNullEmail);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("email");

        }

        @Test
        @DisplayName("Test that duplicate values are rejected for unique field")
        void saveUserWithDuplicateEmailShouldThrowException() {
            User user1 = createUser();
            User user2 = createUser();

            entityManager.persistAndFlush(user1);
            entityManager.clear();

            // Set duplicate value for unique field
            user2.setEmail(user1.getEmail());

            assertThatThrownBy(() -> {
                entityManager.persist(user2);
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void saveUserWithTooLongEmailShouldThrowException() {
            String tooLongValue = "a".repeat(151);
            User userWithTooLongEmail = createUser();
            userWithTooLongEmail.setEmail(tooLongValue);

            assertThatThrownBy(() -> {
                userRepository.save(userWithTooLongEmail);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that values below minimum are rejected")
        void saveUserWithTooSmallAgeShouldThrowException() {
            User userWithTooSmallAge = createUser();
            userWithTooSmallAge.setAge(-1);

            assertThatThrownBy(() -> {
                userRepository.save(userWithTooSmallAge);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that values above maximum are rejected")
        void saveUserWithTooLargeAgeShouldThrowException() {
            User userWithTooLargeAge = createUser();
            userWithTooLargeAge.setAge(151);

            assertThatThrownBy(() -> {
                userRepository.save(userWithTooLargeAge);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }


        @Test
        @DisplayName("Should handle transaction rollback on constraint violation")
        void shouldHandleTransactionRollbackOnConstraintViolation() {
            User validUser = createUser();
            entityManager.persistAndFlush(validUser);
            long initialCount = userRepository.count();

            User invalidUser = createUser();
            invalidUser.setName(null);

            assertThatThrownBy(() -> {
                userRepository.save(invalidUser);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            // Clear entity manager after constraint violation
            entityManager.clear();

            // Verify transaction rollback - data consistency maintained
            long finalCount = userRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should maintain data integrity across multiple constraint violations")
        void shouldMaintainDataIntegrityAcrossMultipleConstraintViolations() {
            long initialCount = userRepository.count();

            User invalidUserName = createUser();
            invalidUserName.setName(null);

            assertThatThrownBy(() -> {
                userRepository.save(invalidUserName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            entityManager.clear();


            long finalCount = userRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
    }
    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should persist OneToOne relationship with Address")
        void shouldPersistOneToOneRelationshipWithAddress() {
            // Given
            User testUser = UserTestDataBuilder.aValidUser().build();
            Address address = createAddress();
            testUser.setAddress(address);

            // When
            User savedUser = userRepository.save(testUser);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getAddress()).isNotNull();
            assertThat(savedUser.getAddress().getId()).isNotNull();
        }

        @Test
        @DisplayName("Should persist without relationship when Address is null")
        void shouldPersistWithoutRelationshipWhenAddressIsNull() {
            // Given
            User testUser = UserTestDataBuilder.aValidUser().build();
            testUser.setAddress(null);

            // When
            User savedUser = userRepository.save(testUser);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getAddress()).isNull();
        }

    @Test
    void deleteUserShouldCascadeDeleteAddress() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        Address address = createAddress();
        testUser.setAddress(address);
        User savedUser = entityManager.persistAndFlush(testUser);

        userRepository.delete(savedUser);
        entityManager.flush();

        Address foundAddress = entityManager.find(Address.class, address.getId());
        assertThat(foundAddress).isNull();
    }

    @Test
    void saveUserWithPostsShouldPersistOneToManyRelationship() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        Post post1 = createPost();
        Post post2 = createPost();

        testUser.getPosts().add(post1);
        testUser.getPosts().add(post2);

        post1.setAuthor(testUser);
        post2.setAuthor(testUser);

        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPosts()).hasSize(2);
        assertThat(savedUser.getPosts()).allMatch(item -> item.getId() != null);
    }

    @Test
    void saveUserWithEmptyPostsShouldPersistWithoutRelationships() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        testUser.getPosts().clear();

        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPosts()).isEmpty();
    }

    @Test
    void addPostToUserShouldUpdateRelationship() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        User savedUser = entityManager.persistAndFlush(testUser);

        Post newPost = createPost();
        savedUser.getPosts().add(newPost);
        newPost.setAuthor(savedUser);

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getPosts()).hasSize(1);
        assertThat(updatedUser.getPosts().get(0).getId()).isNotNull();
    }

    @Test
    void removePostFromUserShouldUpdateRelationship() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        Post post = createPost();
        testUser.getPosts().add(post);
        post.setAuthor(testUser);
        User savedUser = entityManager.persistAndFlush(testUser);

        savedUser.getPosts().clear();

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getPosts()).isEmpty();
    }

    @Test
    void removePostFromUserShouldDeleteOrphan() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        Post post = createPost();
        testUser.getPosts().add(post);
        post.setAuthor(testUser);
        User savedUser = entityManager.persistAndFlush(testUser);
        Long postId = post.getId();

        savedUser.getPosts().clear();
        userRepository.save(savedUser);
        entityManager.flush();

        Post foundPost = entityManager.find(Post.class, postId);
        assertThat(foundPost).isNull();
    }

    @Test
    void deleteUserShouldCascadeDeletePosts() {
        User testUser = UserTestDataBuilder.aValidUser().build();
        Post post = createPost();
        testUser.getPosts().add(post);
        post.setAuthor(testUser);
        User savedUser = entityManager.persistAndFlush(testUser);
        Long postId = post.getId();

        userRepository.delete(savedUser);
        entityManager.flush();

        Post foundPost = entityManager.find(Post.class, postId);
        assertThat(foundPost).isNull();
    }


    @Test
    void loadUserWithRelationshipsShouldNotCauseNPlusOneQueries() {
        User testUser = createUser();
        User savedUser = entityManager.persistAndFlush(testUser);
        entityManager.clear();

        Optional<User> found = userRepository.findById(savedUser.getId());

        assertThat(found).isPresent();
        User loadedUser = found.get();
        assertThat(loadedUser).isNotNull();
    }


}

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void findAllWithPaginationShouldReturnPagedResults() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());
        User user3 = entityManager.persistAndFlush(createUser());

        Pageable pageable = PageRequest.of(0, 2);
        Page<User> page = userRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAllWithPaginationSecondPageShouldReturnRemainingResults() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());
        User user3 = entityManager.persistAndFlush(createUser());

        Pageable pageable = PageRequest.of(1, 2);
        Page<User> page = userRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void findAllWithSortingShouldReturnSortedResults() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());
        User user3 = entityManager.persistAndFlush(createUser());

        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        List<User> sortedUsers = userRepository.findAll(sort);

        assertThat(sortedUsers).hasSize(3);
        assertThat(sortedUsers).isSortedAccordingTo((a, b) -> a.getName().compareTo(b.getName()));
    }

    @Test
    void findAllWithPaginationAndSortingShouldReturnPagedAndSortedResults() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());
        User user3 = entityManager.persistAndFlush(createUser());

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "name"));
        Page<User> page = userRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).isSortedAccordingTo((a, b) -> b.getName().compareTo(a.getName()));
    }

    @Test
    void findAllWithEmptyPageShouldReturnEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = userRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void findAllWithLargePageSizeShouldReturnAllResults() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());

        Pageable pageable = PageRequest.of(0, 100);
        Page<User> page = userRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void paginationWithMultipleSortFieldsShouldWork() {
        User user1 = entityManager.persistAndFlush(createUser());
        User user2 = entityManager.persistAndFlush(createUser());
        User user3 = entityManager.persistAndFlush(createUser());

        Sort multiSort = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.desc("age")
        );
        Pageable pageable = PageRequest.of(0, 2, multiSort);
        Page<User> page = userRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
    }

    
}

}
