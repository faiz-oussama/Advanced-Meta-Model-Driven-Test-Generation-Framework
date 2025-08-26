package com.univade.TU.repository;

import com.univade.TU.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    // finder methods
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    List<User> findByAge(Integer age);
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);
    List<User> findByAddressCity(String city);

    // finder methods with query
    @Query("SELECT DISTINCT u FROM User u WHERE SIZE(u.posts) > 0")
    List<User> findUsersWithPosts();

    @Query("SELECT u FROM User u WHERE SIZE(u.posts) = 0")
    List<User> findUsersWithoutPosts();

    Page<User> findByAddressCity(String city, Pageable pageable);
    Page<User> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable);
    List<User> findAllByOrderByAgeDesc();

    // existence methods
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByAddressCity(String city);

    // deletion methods
    void deleteById(@NonNull Long id);
    void deleteByEmail(String email);
    void deleteByAddressCity(String city);

        @Query("SELECT u FROM User u WHERE u.age > :minAge AND u.address.city = :city")
    List<User> findUsersOlderThanInCity(@Param("minAge") Integer minAge, @Param("city") String city);

    @Query("SELECT u FROM User u WHERE SIZE(u.posts) >= :minPosts")
    List<User> findUsersWithMinimumPosts(@Param("minPosts") int minPosts);

    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findUsersByEmailDomain(@Param("domain") String domain);

    @Query("SELECT DISTINCT u FROM User u JOIN u.posts p WHERE p.title LIKE %:keyword%")
    List<User> findUsersWithPostsContainingKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge AND u.address.city IN :cities")
    List<User> findUsersByAgeRangeAndCities(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge, @Param("cities") List<String> cities);

    @Query("SELECT COUNT(u) FROM User u WHERE u.address.city = :city")
    long countUsersByCity(@Param("city") String city);

    @Query("SELECT u.address.city, COUNT(u) FROM User u GROUP BY u.address.city")
    List<Object[]> getUserCountByCity();

    @Query("SELECT AVG(u.age) FROM User u WHERE u.address.city = :city")
    Double getAverageAgeByCity(@Param("city") String city);

    @Query("SELECT u FROM User u WHERE u.age = (SELECT MAX(u2.age) FROM User u2)")
    List<User> findOldestUsers();

    @Query("SELECT u FROM User u WHERE u.age = (SELECT MIN(u2.age) FROM User u2)")
    List<User> findYoungestUsers();
}
