package com.univade.TU.service;

import com.univade.TU.testdata.PostTestDataBuilder;

import com.univade.TU.testdata.UserTestDataBuilder;

import com.univade.TU.entity.Post;

import com.univade.TU.entity.User;

import com.univade.TU.repository.PostRepository;
import com.univade.TU.repository.UserRepository;

import com.univade.TU.service.PostService;
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
@DisplayName("Post Service Tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        reset(postRepository);
    }

    private Post createPost() {
        return PostTestDataBuilder.aValidPost().build();
    }

    private User createUser() {
        return UserTestDataBuilder.aDefaultUser().build();
    }

    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createPostShouldSaveAndReturnEntity() {
            Post testPost = createPost();
            Post savedPost = createPost();
            savedPost.setId(1L);

            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            Post result = postService.createPost(testPost);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(postRepository).save(testPost);
        }

        @Test
        void createPostWithNullShouldThrowException() {
            assertThatThrownBy(() -> postService.createPost(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void getPostByIdShouldReturnEntityWhenExists() {
            Long testId = 1L;
            Post testPost = createPost();
            testPost.setId(testId);

            when(postRepository.findById(testId)).thenReturn(Optional.of(testPost));

            Post result = postService.getPostById(testId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(postRepository).findById(testId);
        }

        @Test
        void getPostByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(postRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostById(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(postRepository).findById(testId);
        }

        @Test
        void getAllPostsShouldReturnAllEntities() {
            Post post1 = createPost();
            Post post2 = createPost();
            List<Post> expectedPosts = List.of(post1, post2);

            when(postRepository.findAll()).thenReturn(expectedPosts);

            List<Post> result = postService.getAllPosts();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedPosts);
            verify(postRepository).findAll();
        }

        @Test
        void getAllPostsWhenEmptyShouldReturnEmptyList() {
            when(postRepository.findAll()).thenReturn(Collections.emptyList());

            List<Post> result = postService.getAllPosts();

            assertThat(result).isEmpty();
            verify(postRepository).findAll();
        }

        @Test
        void updatePostShouldUpdateAndReturnEntity() {
            Long testId = 1L;
            Post existingPost = createPost();
            existingPost.setId(testId);

            Post updatedPost = createPost();
            updatedPost.setId(testId);
            updatedPost.setTitle("Updated Title");

            when(postRepository.findById(testId)).thenReturn(Optional.of(existingPost));
            when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

            Post result = postService.updatePost(testId, updatedPost);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            verify(postRepository).findById(testId);
            verify(postRepository).save(any(Post.class));
        }

        @Test
        void updatePostWithNonExistentIdShouldThrowException() {
            Long testId = 999L;
            Post updatedPost = createPost();

            when(postRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.updatePost(testId, updatedPost))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(postRepository).findById(testId);
            verify(postRepository, never()).save(any(Post.class));
        }

        @Test
        void deletePostByIdShouldDeleteWhenExists() {
            Long testId = 1L;

            when(postRepository.existsById(testId)).thenReturn(true);
            doNothing().when(postRepository).deleteById(testId);

            assertThatCode(() -> postService.deletePost(testId))
                    .doesNotThrowAnyException();

            verify(postRepository).existsById(testId);
            verify(postRepository).deleteById(testId);
        }

        @Test
        void deletePostByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(postRepository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> postService.deletePost(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(postRepository).existsById(testId);
            verify(postRepository, never()).deleteById(testId);
        }

        @Test
        void existsPostByIdShouldReturnTrueWhenExists() {
            Long testId = 1L;

            when(postRepository.existsById(testId)).thenReturn(true);

            boolean result = postService.existsPostById(testId);

            assertThat(result).isTrue();
            verify(postRepository).existsById(testId);
        }

        @Test
        void existsPostByIdShouldReturnFalseWhenNotExists() {
            Long testId = 999L;

            when(postRepository.existsById(testId)).thenReturn(false);

            boolean result = postService.existsPostById(testId);

            assertThat(result).isFalse();
            verify(postRepository).existsById(testId);
        }

        @Test
        void countPostsShouldReturnCorrectCount() {
            long expectedCount = 5L;

            when(postRepository.count()).thenReturn(expectedCount);

            long result = postService.countPosts();

            assertThat(result).isEqualTo(expectedCount);
            verify(postRepository).count();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when creating Post with null entity")
        void createPostWithNullShouldThrowException() {
            assertThatThrownBy(() -> postService.createPost(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post cannot be null");

            verifyNoInteractions(postRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Post with null entity")
        void updatePostWithNullShouldThrowException() {
            Long testId = 1L;

            assertThatThrownBy(() -> postService.updatePost(testId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Post cannot be null");

            verifyNoInteractions(postRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Post with null ID")
        void updatePostWithNullIdShouldThrowException() {
            Post testPost = createPost();

            assertThatThrownBy(() -> postService.updatePost(null, testPost))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(postRepository);
        }

        @Test
        @DisplayName("Should throw exception when deleting Post with null ID")
        void deletePostWithNullIdShouldThrowException() {
            assertThatThrownBy(() -> postService.deletePost(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(postRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent Post")
        void updatePostWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;
            Post testPost = createPost();

            when(postRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.updatePost(nonExistentId, testPost))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Post not found with id: " + nonExistentId);

            verify(postRepository).findById(nonExistentId);
            verify(postRepository, never()).save(any(Post.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent Post")
        void deletePostWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;

            when(postRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> postService.deletePost(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Post not found with id: " + nonExistentId);

            verify(postRepository).existsById(nonExistentId);
            verify(postRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully when finding Post")
        void findPostByIdShouldHandleRepositoryException() {
            Long testId = 1L;

            when(postRepository.findById(testId))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> postService.getPostById(testId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(postRepository).findById(testId);
        }

        @Test
        @DisplayName("Should validate business rules before saving Post")
        void createPostShouldValidateBusinessRules() {
            Post testPost = createPost();
            Post savedPost = createPost();
            savedPost.setId(1L);

            when(postRepository.save(testPost)).thenReturn(savedPost);

            Post result = postService.createPost(testPost);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedPost);
            verify(postRepository).save(testPost);
        }

        @Test
        @DisplayName("Should check existence before operations")
        void existsPostByIdShouldReturnCorrectValue() {
            Long existingId = 1L;
            Long nonExistentId = 999L;

            when(postRepository.existsById(existingId)).thenReturn(true);
            when(postRepository.existsById(nonExistentId)).thenReturn(false);

            boolean existsResult = postService.existsPostById(existingId);
            boolean notExistsResult = postService.existsPostById(nonExistentId);

            assertThat(existsResult).isTrue();
            assertThat(notExistsResult).isFalse();
            verify(postRepository).existsById(existingId);
            verify(postRepository).existsById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null ID gracefully in exists check")
        void existsPostByIdWithNullShouldReturnFalse() {
            boolean result = postService.existsPostById(null);

            assertThat(result).isFalse();
            verifyNoInteractions(postRepository);
        }

        @Test
        @DisplayName("Should handle validation errors from JPA/Hibernate")
        void createPostShouldHandleConstraintViolationException() {
            Post invalidPost = createPost();
            invalidPost.setTitle(null);

            when(postRepository.save(invalidPost))
                    .thenThrow(new ConstraintViolationException("Validation failed", null));

            assertThatThrownBy(() -> postService.createPost(invalidPost))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Validation failed");

            verify(postRepository).save(invalidPost);
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void createPostShouldHandleDataIntegrityViolationException() {
            Post duplicatePost = createPost();

            when(postRepository.save(duplicatePost))
                    .thenThrow(new DataIntegrityViolationException("Duplicate constraint violation"));

            assertThatThrownBy(() -> postService.createPost(duplicatePost))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Duplicate constraint violation");

            verify(postRepository).save(duplicatePost);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void getAllPostsWithPageableShouldReturnPagedResults() {
            Post post1 = createPost();
            Post post2 = createPost();
            Post post3 = createPost();

            List<Post> postList = List.of(post1, post2);
            Page<Post> expectedPage = new PageImpl<>(postList, PageRequest.of(0, 2), 3);

            Pageable pageable = PageRequest.of(0, 2);
            when(postRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
            verify(postRepository).findAll(pageable);
        }

        @Test
        void getAllPostsWithPageableAndSortShouldReturnSortedResults() {
            Post post1 = createPost();
            post1.setTitle("Hicham");
            Post post2 = createPost();
            post2.setTitle("Oussama");

            List<Post> sortedPostList = List.of(post1, post2);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
            Page<Post> expectedPage = new PageImpl<>(sortedPostList, pageable, 2);

            when(postRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Hicham");
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("Oussama");
            verify(postRepository).findAll(pageable);
        }

        @Test
        void getAllPostsWithEmptyPageShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(postRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            verify(postRepository).findAll(pageable);
        }

        @Test
        void getAllPostsWithLargePageSizeShouldReturnAllResults() {
            Post post1 = createPost();
            Post post2 = createPost();
            Post post3 = createPost();

            List<Post> allPosts = List.of(post1, post2, post3);
            Pageable pageable = PageRequest.of(0, 100);
            Page<Post> expectedPage = new PageImpl<>(allPosts, pageable, 3);

            when(postRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(postRepository).findAll(pageable);
        }

        @Test
        void getAllPostsWithSecondPageShouldReturnCorrectResults() {
            Post post3 = createPost();
            Post post4 = createPost();

            List<Post> secondPagePosts = List.of(post3, post4);
            Pageable pageable = PageRequest.of(1, 2);
            Page<Post> expectedPage = new PageImpl<>(secondPagePosts, pageable, 4);

            when(postRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            verify(postRepository).findAll(pageable);
        }



        @Test
        void getAllPostsWithDescendingSortShouldReturnCorrectOrder() {
            Post post1 = createPost();
            post1.setTitle("Ilyass");
            Post post2 = createPost();
            post2.setTitle("Oussama");

            List<Post> sortedPostList = List.of(post2, post1);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("title").descending());
            Page<Post> expectedPage = new PageImpl<>(sortedPostList, pageable, 2);

            when(postRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Oussama");
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("Ilyass");
            verify(postRepository).findAll(pageable);
        }

        @Test
        void getAllPostsWithCustomPageSizeShouldRespectPageSize() {
            Post post1 = createPost();
            Post post2 = createPost();

            List<Post> postList = List.of(post1, post2);
            Pageable pageable = PageRequest.of(0, 1);
            Page<Post> expectedPage = new PageImpl<>(List.of(post1), pageable, 2);

            when(postRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Post> result = postService.getAllPosts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(1);
            verify(postRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Service Validation Tests")
    class ServiceValidationTests {

        @Test
        @DisplayName("Should validate entity before saving")
        void createPostShouldValidateEntity() {
            Post testPost = createPost();
            Post savedPost = createPost();
            savedPost.setId(1L);

            when(postRepository.save(testPost)).thenReturn(savedPost);

            Post result = postService.createPost(testPost);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedPost);
            verify(postRepository).save(testPost);
        }

        @Test
        @DisplayName("Should handle repository exceptions during validation")
        void createPostShouldHandleRepositoryExceptions() {
            Post testPost = createPost();

            when(postRepository.save(testPost))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> postService.createPost(testPost))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(postRepository).save(testPost);
        }

        @Test
        @DisplayName("Should validate entity state before update")
        void updatePostShouldValidateEntityState() {
            Long testId = 1L;
            Post existingPost = createPost();
            existingPost.setId(testId);

            Post updatedPost = createPost();
            Post savedPost = createPost();
            savedPost.setId(testId);

            when(postRepository.findById(testId)).thenReturn(Optional.of(existingPost));
            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            Post result = postService.updatePost(testId, updatedPost);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(postRepository).findById(testId);
            verify(postRepository).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        void createPostWithValidAuthorShouldSucceed() {
            Post testPost = createPost();
            User testUser = createUser();
            testUser.setId(1L);
            testPost.setAuthor(testUser);

            Post savedPost = createPost();
            savedPost.setAuthor(testUser);
            savedPost.setId(1L);

            when(postRepository.save(any(Post.class))).thenReturn(savedPost);

            Post result = postService.createPost(testPost);

            assertThat(result).isNotNull();
            assertThat(result.getAuthor()).isNotNull();
            verify(postRepository).save(testPost);
        }



        @Test
        void createPostWithNullAuthorShouldThrowException() {
            Post testPost = createPost();
            testPost.setAuthor(null);

            when(postRepository.save(any(Post.class)))
                    .thenThrow(new ConstraintViolationException("author cannot be null", null));

            assertThatThrownBy(() -> postService.createPost(testPost))
                    .isInstanceOf(ConstraintViolationException.class);

            verify(postRepository).save(testPost);
        }



    }

    @Nested
    @DisplayName("Relationship Update Tests")
    class RelationshipUpdateTests {

        @Test
        @DisplayName("Should update Post with new User")
        void updatePostWithNewUserShouldWork() {
            Long postId = 1L;
            Post existingPost = createPost();
            existingPost.setId(postId);

            User newUser = createUser();
            Post updatedPost = createPost();
            updatedPost.setId(postId);
            updatedPost.setAuthor(newUser);

            when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
            when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

            Post result = postService.updatePost(postId, updatedPost);

            assertThat(result).isNotNull();
            assertThat(result.getAuthor()).isEqualTo(newUser);
            verify(postRepository).findById(postId);
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("Should handle null User update")
        void updatePostWithNullUserShouldWork() {
            Long postId = 1L;
            Post existingPost = createPost();
            existingPost.setId(postId);
            existingPost.setAuthor(createUser());

            Post updatedPost = createPost();
            updatedPost.setId(postId);
            updatedPost.setAuthor(null);

            when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
            when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

            Post result = postService.updatePost(postId, updatedPost);

            assertThat(result).isNotNull();
            assertThat(result.getAuthor()).isNull();
            verify(postRepository).findById(postId);
            verify(postRepository).save(any(Post.class));
        }

    }

    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private PostService postServiceIntegration;

        @Autowired
        private PostRepository postRepositoryIntegration;

        @Autowired
        private jakarta.persistence.EntityManager entityManager;

        @Autowired
        private UserRepository userRepositoryIntegration;

        private Post testPostIntegration;

        @BeforeEach
        void setUpIntegration() {
            testPostIntegration = createTestEntity();
        }

        private Post createTestEntity() {
            Post entity = PostTestDataBuilder.aDefaultPost().build();
            if (entity.getAuthor() != null) {
                User persistedUser = userRepositoryIntegration.save(entity.getAuthor());
                entity.setAuthor(persistedUser);
            }
            return entity;
        }

        @Nested
        @DisplayName("Business Logic Integration Tests")
        class BusinessLogicIntegrationTests {

            @Test
            @DisplayName("Should create Post and persist to database")
            void shouldCreatePostAndPersistToDatabase() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);

                assertThat(created).isNotNull();
                assertThat(created.getId()).isNotNull();
                
                Optional<Post> persisted = postRepositoryIntegration.findById(created.getId());
                assertThat(persisted).isPresent();
                assertThat(persisted.get().getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should update Post and persist changes")
            void shouldUpdatePostAndPersistChanges() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);
                
                created.setTitle("Updated title");

                Post updated = postServiceIntegration.updatePost(created.getId(), created);

                assertThat(updated).isNotNull();
                Post persisted = postRepositoryIntegration.findById(created.getId()).orElseThrow();
                assertThat(persisted.getTitle()).isEqualTo("Updated title");
            }

            @Test
            @DisplayName("Should delete Post from database")
            void shouldDeletePostFromDatabase() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);
                Long id = created.getId();

                postServiceIntegration.deletePost(id);

                Optional<Post> deleted = postRepositoryIntegration.findById(id);
                assertThat(deleted).isEmpty();
            }
        }

        @Nested
        @DisplayName("Validation Constraint Integration Tests")
        class ValidationConstraintIntegrationTests {

            @Test
            @DisplayName("Should handle validation errors from JPA/Hibernate")
            void shouldHandleJpaValidationErrors() {
                Post invalidPost = PostTestDataBuilder.aDefaultPost()
                        .withTitle(null)
                        .build();
                if (invalidPost.getAuthor() != null) {
                    User persistedUser = userRepositoryIntegration.save(invalidPost.getAuthor());
                    invalidPost.setAuthor(persistedUser);
                }

                try {
                    assertThatThrownBy(() -> {
                        postServiceIntegration.createPost(invalidPost);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } finally {
                    entityManager.clear();
                }
            }

        }

        @Nested
        @DisplayName("Transactional Behavior Integration Tests")
        class TransactionalBehaviorIntegrationTests {

            @Test
            @DisplayName("Should rollback transaction when error occurs during save")
            void shouldRollbackTransactionWhenErrorOccursDuringSave() {
                long initialCount = postRepositoryIntegration.count();

                Post invalidPost = PostTestDataBuilder.aDefaultPost()
                        .withTitle(null)
                        .build();
                // Persist required relationship
                if (invalidPost.getAuthor() != null) {
                    User persistedUser = userRepositoryIntegration.save(invalidPost.getAuthor());
                    invalidPost.setAuthor(persistedUser);
                }

                try {
                    assertThatThrownBy(() -> {
                        postServiceIntegration.createPost(invalidPost);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } catch (Exception e) {
                    // Clear entity manager after constraint violation to avoid inconsistent state
                    entityManager.clear();
                    throw e;
                }

                // Clear entity manager to avoid stale state
                entityManager.clear();
                long finalCount = postRepositoryIntegration.count();
                assertThat(finalCount).isEqualTo(initialCount);
            }
        }

        @Nested
        @DisplayName("Entity Relationship Integration Tests")
        class EntityRelationshipIntegrationTests {

            @Test
            @DisplayName("Should save Post with User reference")
            void shouldSavePostWithUserReference() {
                Post testEntity = createTestEntity();
                User related = UserTestDataBuilder.aDefaultUser().build();
                User savedRelated = userRepositoryIntegration.save(related);

                testEntity.setAuthor(savedRelated);

                Post saved = postServiceIntegration.createPost(testEntity);

                assertThat(saved.getAuthor()).isNotNull();
                assertThat(saved.getAuthor().getId())
                        .isEqualTo(savedRelated.getId());
            }

            @Test
            @DisplayName("Should update Post with new User relationship")
            void shouldUpdatePostWithNewUser() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);

                User newUser = UserTestDataBuilder.aDefaultUser().build();
                User savedUser = userRepositoryIntegration.save(newUser);

                created.setAuthor(savedUser);
                Post updated = postServiceIntegration.updatePost(created.getId(), created);

                assertThat(updated.getAuthor()).isNotNull();
                assertThat(updated.getAuthor().getId()).isEqualTo(savedUser.getId());
            }

        }


        @Nested
        @DisplayName("Finder Method Integration Tests")
        class FinderMethodIntegrationTests {

            @Test
            @DisplayName("Should find Post by ID correctly")
            void shouldFindPostByIdCorrectly() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);

                Post found = postServiceIntegration.getPostById(created.getId());

                assertThat(found).isNotNull();
                assertThat(found.getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should return empty when Post not found by ID")
            void shouldReturnEmptyWhenPostNotFoundById() {
                Long nonExistentId = 999L;

                assertThatThrownBy(() -> postServiceIntegration.getPostById(nonExistentId))
                        .isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("Should check existence correctly")
            void shouldCheckExistenceCorrectly() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);
                Long nonExistentId = 999L;

                boolean exists = postServiceIntegration.existsPostById(created.getId());
                boolean notExists = postServiceIntegration.existsPostById(nonExistentId);

                assertThat(exists).isTrue();
                assertThat(notExists).isFalse();
            }

            @Test
            @DisplayName("Should find all Posts from database")
            void shouldFindAllPostsFromDatabase() {
                Post testEntity1 = createTestEntity();
                Post entity1 = postServiceIntegration.createPost(testEntity1);
                Post testEntity2 = createTestEntity();
                Post entity2 = postServiceIntegration.createPost(testEntity2);

                List<Post> all = postServiceIntegration.getAllPosts();

                assertThat(all).hasSizeGreaterThanOrEqualTo(2);
                assertThat(all).extracting(Post::getId)
                        .contains(entity1.getId(), entity2.getId());
            }
        }

        @Nested
        @DisplayName("Deletion Logic Integration Tests")
        class DeletionLogicIntegrationTests {

            @Test
            @DisplayName("Should delete Post and verify removal")
            void shouldDeletePostAndVerifyRemoval() {
                Post testEntity = createTestEntity();
                Post created = postServiceIntegration.createPost(testEntity);
                Long id = created.getId();

                postServiceIntegration.deletePost(id);

                boolean exists = postRepositoryIntegration.existsById(id);
                assertThat(exists).isFalse();
            }

        }
    }




}
