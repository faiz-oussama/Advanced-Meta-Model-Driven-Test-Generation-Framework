package com.univade.TU.repository;

import com.univade.TU.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    List<Category> findByActive(Boolean active);
    List<Category> findByNameContaining(String nameFragment);
}
