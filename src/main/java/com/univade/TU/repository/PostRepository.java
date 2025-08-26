package com.univade.TU.repository;

import com.univade.TU.entity.Post;
import com.univade.TU.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // finder methods
    List<Post> findByAuthor(User author);
    Page<Post> findByAuthor(User author, Pageable pageable);
    List<Post> findByAuthorId(Long authorId);
    Optional<Post> findByTitle(String title);

    // finder methods with query
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Post> searchByTitleOrContent(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Post> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author.name = :authorName")
    List<Post> findByAuthorName(@Param("authorName") String authorName);

    @Query("SELECT p FROM Post p WHERE p.author.email = :authorEmail")
    List<Post> findByAuthorEmail(@Param("authorEmail") String authorEmail);

    List<Post> findAllByOrderByTitleAsc();
    List<Post> findByAuthorOrderByTitleAsc(User author);

    // existence methods
    boolean existsByTitle(String title);
    boolean existsByAuthor(User author);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Post p WHERE LOWER(CAST(p.content AS string)) LIKE LOWER(CONCAT('%', :content, '%'))")
    boolean existsByContentContainingIgnoreCase(@Param("content") String content);

    // deletion methods
    void deleteByAuthor(User author);
    void deleteByTitleContainingIgnoreCase(String title);
    @Query("DELETE FROM Post p WHERE p.author.address.city = :city")
    void deleteByAuthorCity(@Param("city") String city);
}
