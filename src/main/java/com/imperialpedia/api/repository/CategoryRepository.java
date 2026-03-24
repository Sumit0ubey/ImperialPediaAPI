package com.imperialpedia.api.repository;

import com.imperialpedia.api.entity.term.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByNameAsc();
    List<Category> findByNameStartingWithIgnoreCaseOrderByNameAsc(String prefix);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Integer id);

}

