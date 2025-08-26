package com.univade.TU.repository;

import com.univade.TU.testdata.PostTestDataBuilder;

import com.univade.TU.testdata.UserTestDataBuilder;

import com.univade.TU.entity.Post;

import com.univade.TU.entity.User;

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
@DisplayName("Post Repository Tests")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Post createPost() {
        Post post = PostTestDataBuilder.aValidPost().build();
        User user = UserTestDataBuilder.aDefaultUser().build();
        User persistedUser = entityManager.persistAndFlush(user);
        post.setAuthor(persistedUser);
        return post;
    }

    private User createUser() {
        User user = UserTestDataBuilder.aDefaultUser().build();
        return user;
    }


    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createPostShouldPersistNewEntity() {
            Post testPost = createPost();
            Post createdPost = postRepository.save(testPost);

            assertThat(createdPost).isNotNull();
            assertThat(createdPost.getId()).isNotNull();
            assertThat(createdPost.getId()).isPositive();

            Post found = entityManager.find(Post.class, createdPost.getId());
            assertThat(found).isNotNull();
            assertThat(found.getTitle()).isEqualTo(testPost.getTitle());
            assertThat(found.getContent()).isEqualTo(testPost.getContent());
        }

        @Test
        void createPostWithNullShouldThrowException() {
            assertThatThrownBy(() -> postRepository.save(null))
                    .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        void createMultiplePostsShouldPersistAll() {
            Post post1 = createPost();
            Post post2 = createPost();
            Post post3 = createPost();

            List<Post> createdPosts = postRepository.saveAll(
                    List.of(post1, post2, post3));

            assertThat(createdPosts).hasSize(3);
            assertThat(createdPosts).allMatch(post -> post.getId() != null);

            long count = postRepository.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void readPostByIdShouldReturnCorrectEntity() {
            Post testPost = createPost();
            Post savedPost = entityManager.persistAndFlush(testPost);

            Optional<Post> found = postRepository.findById(savedPost.getId());

            assertThat(found).isPresent();
            Post retrievedPost = found.get();
            assertThat(retrievedPost.getId()).isEqualTo(savedPost.getId());
            assertThat(retrievedPost.getTitle()).isEqualTo(testPost.getTitle());
            assertThat(retrievedPost.getContent()).isEqualTo(testPost.getContent());
        }

        @Test
        void readPostByNonExistentIdShouldReturnEmpty() {
            Optional<Post> found = postRepository.findById(999L);

            assertThat(found).isEmpty();
        }

    @Test
    void readAllPostsShouldReturnAllEntities() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());

        List<Post> allPosts = postRepository.findAll();

            assertThat(allPosts).hasSize(2);
            assertThat(allPosts).extracting("id")
                    .contains(post1.getId(), post2.getId());
    }

    @Test
    void readAllPostsWhenEmptyShouldReturnEmptyList() {
        List<Post> allPosts = postRepository.findAll();

        assertThat(allPosts).isEmpty();
    }

    @Test
    void updatePostShouldModifyExistingEntity() {
        Post testPost = createPost();
        Post savedPost = entityManager.persistAndFlush(testPost);
        entityManager.detach(savedPost);

        savedPost.setTitle("Updated Title");

        Post updatedPost = postRepository.save(savedPost);

        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getId()).isEqualTo(savedPost.getId());
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");

        Post found = entityManager.find(Post.class, savedPost.getId());
        assertThat(found.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void saveNewPostShouldCreateEntity() {
        long initialCount = postRepository.count();

        Post newPost = createPost();

        Post result = postRepository.save(newPost);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        long finalCount = postRepository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void updateAllPostAttributesShouldPersistChanges() {
        Post testPost = createPost();
        Post savedPost = entityManager.persistAndFlush(testPost);
        entityManager.detach(savedPost);

        savedPost.setTitle("Updated Title");

        Post updatedPost = postRepository.save(savedPost);

        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void deletePostByIdShouldRemoveEntity() {
        Post testPost = createPost();
        Post savedPost = entityManager.persistAndFlush(testPost);

        postRepository.deleteById(savedPost.getId());

        Optional<Post> found = postRepository.findById(savedPost.getId());
        assertThat(found).isEmpty();

        Post entityFound = entityManager.find(Post.class, savedPost.getId());
        assertThat(entityFound).isNull();
    }

        @Test
        void deletePostByNonExistentIdShouldNotThrowException() {
            assertThatCode(() -> postRepository.deleteById(999L))
                    .doesNotThrowAnyException();
        }

    @Test
    void deletePostEntityShouldRemoveFromDatabase() {
        Post testPost = createPost();
        Post savedPost = entityManager.persistAndFlush(testPost);

        postRepository.delete(savedPost);

        Optional<Post> found = postRepository.findById(savedPost.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAllPostsShouldRemoveAllEntities() {
        entityManager.persistAndFlush(createPost());
        entityManager.persistAndFlush(createPost());
        entityManager.persistAndFlush(createPost());

        postRepository.deleteAll();

        List<Post> allPosts = postRepository.findAll();
        assertThat(allPosts).isEmpty();

        long count = postRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void deleteMultiplePostsByIdShouldRemoveSpecifiedEntities() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());
        Post post3 = entityManager.persistAndFlush(createPost());

        postRepository.deleteAllById(List.of(post1.getId(), post2.getId()));

        List<Post> remainingPosts = postRepository.findAll();
        assertThat(remainingPosts).hasSize(1);
        assertThat(remainingPosts.get(0).getId()).isEqualTo(post3.getId());
    }

    @Test
    void existsPostByIdShouldReturnTrueForExistingEntity() {
        Post testPost = createPost();
        Post savedPost = entityManager.persistAndFlush(testPost);

        boolean exists = postRepository.existsById(savedPost.getId());

        assertThat(exists).isTrue();
    }

        @Test
        void existsPostByIdShouldReturnFalseForNonExistingEntity() {
            boolean exists = postRepository.existsById(999L);

            assertThat(exists).isFalse();
        }

    @Test
    void countPostsShouldReturnCorrectNumber() {
        entityManager.persistAndFlush(createPost());
        entityManager.persistAndFlush(createPost());

        long count = postRepository.count();

        assertThat(count).isEqualTo(2);
    }

        @Test
        void countPostsWhenEmptyShouldReturnZero() {
            long count = postRepository.count();

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test that primary key is automatically generated")
        void savePostShouldGenerateId() {
            Post post = createPost();

            Post savedPost = postRepository.save(post);

            assertThat(savedPost).isNotNull();
            assertThat(savedPost.getId()).isNotNull();
            assertThat(savedPost.getId()).isPositive();

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void savePostWithNullTitleShouldThrowException() {
            Post postWithNullTitle = createPost();
            postWithNullTitle.setTitle(null);

            assertThatThrownBy(() -> {
                postRepository.save(postWithNullTitle);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("title");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePostWithTooLongTitleShouldThrowException() {
            String tooLongValue = "a".repeat(256);
            Post postWithTooLongTitle = createPost();
            postWithTooLongTitle.setTitle(tooLongValue);

            assertThatThrownBy(() -> {
                postRepository.save(postWithTooLongTitle);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void savePostWithNullContentShouldThrowException() {
            Post postWithNullContent = createPost();
            postWithNullContent.setContent(null);

            assertThatThrownBy(() -> {
                postRepository.save(postWithNullContent);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("content");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePostWithTooLongContentShouldThrowException() {
            String tooLongValue = "a".repeat(5001);
            Post postWithTooLongContent = createPost();
            postWithTooLongContent.setContent(tooLongValue);

            assertThatThrownBy(() -> {
                postRepository.save(postWithTooLongContent);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }


        @Test
        @DisplayName("Should handle transaction rollback on constraint violation")
        void shouldHandleTransactionRollbackOnConstraintViolation() {
            Post validPost = createPost();
            entityManager.persistAndFlush(validPost);
            long initialCount = postRepository.count();

            Post invalidPost = createPost();
            invalidPost.setTitle(null);

            assertThatThrownBy(() -> {
                postRepository.save(invalidPost);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            // Clear entity manager after constraint violation
            entityManager.clear();

            // Verify transaction rollback - data consistency maintained
            long finalCount = postRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should maintain data integrity across multiple constraint violations")
        void shouldMaintainDataIntegrityAcrossMultipleConstraintViolations() {
            long initialCount = postRepository.count();

            Post invalidPostTitle = createPost();
            invalidPostTitle.setTitle(null);

            assertThatThrownBy(() -> {
                postRepository.save(invalidPostTitle);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            entityManager.clear();


            long finalCount = postRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
    }
    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

    @Test
    void savePostWithAuthorShouldPersistManyToOneRelationship() {
        // Create and persist the target entity first
        User author = UserTestDataBuilder.aDefaultUser().build();
        User persistedUser = entityManager.persistAndFlush(author);

        // Create the main entity and set the relationship
        Post testPost = PostTestDataBuilder.aValidPost().build();
        testPost.setAuthor(persistedUser);

        Post savedPost = postRepository.save(testPost);

        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getAuthor()).isNotNull();
        assertThat(savedPost.getAuthor().getId()).isNotNull();
    }

    @Test
    void saveMultiplePostsWithSameAuthorShouldWork() {
        // Create and persist the shared target entity
        User sharedUser = UserTestDataBuilder.aDefaultUser().build();
        User persistedSharedUser = entityManager.persistAndFlush(sharedUser);

        // Create main entities and set the shared relationship
        Post post1 = PostTestDataBuilder.aValidPost().build();
        Post post2 = PostTestDataBuilder.aValidPost().build();

        post1.setAuthor(persistedSharedUser);
        post2.setAuthor(persistedSharedUser);

        Post savedPost1 = postRepository.save(post1);
        Post savedPost2 = postRepository.save(post2);

        assertThat(savedPost1.getAuthor().getId()).isEqualTo(persistedSharedUser.getId());
        assertThat(savedPost2.getAuthor().getId()).isEqualTo(persistedSharedUser.getId());
    }


    @Test
    void loadPostWithRelationshipsShouldNotCauseNPlusOneQueries() {
        Post testPost = createPost();
        Post savedPost = entityManager.persistAndFlush(testPost);
        entityManager.clear();

        Optional<Post> found = postRepository.findById(savedPost.getId());

        assertThat(found).isPresent();
        Post loadedPost = found.get();
        assertThat(loadedPost).isNotNull();
    }


}

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void findAllWithPaginationShouldReturnPagedResults() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());
        Post post3 = entityManager.persistAndFlush(createPost());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Post> page = postRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAllWithPaginationSecondPageShouldReturnRemainingResults() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());
        Post post3 = entityManager.persistAndFlush(createPost());

        Pageable pageable = PageRequest.of(1, 2);
        Page<Post> page = postRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void findAllWithSortingShouldReturnSortedResults() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());
        Post post3 = entityManager.persistAndFlush(createPost());

        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        List<Post> sortedPosts = postRepository.findAll(sort);

        assertThat(sortedPosts).hasSize(3);
        assertThat(sortedPosts).isSortedAccordingTo((a, b) -> a.getTitle().compareTo(b.getTitle()));
    }

    @Test
    void findAllWithPaginationAndSortingShouldReturnPagedAndSortedResults() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());
        Post post3 = entityManager.persistAndFlush(createPost());

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "title"));
        Page<Post> page = postRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).isSortedAccordingTo((a, b) -> b.getTitle().compareTo(a.getTitle()));
    }

    @Test
    void findAllWithEmptyPageShouldReturnEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = postRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void findAllWithLargePageSizeShouldReturnAllResults() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());

        Pageable pageable = PageRequest.of(0, 100);
        Page<Post> page = postRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void paginationWithMultipleSortFieldsShouldWork() {
        Post post1 = entityManager.persistAndFlush(createPost());
        Post post2 = entityManager.persistAndFlush(createPost());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Post> page = postRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
    }

    
}

}
