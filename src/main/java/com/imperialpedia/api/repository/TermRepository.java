package com.imperialpedia.api.repository;

import com.imperialpedia.api.entity.term.Term;
import com.imperialpedia.api.entity.term.TermStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermRepository extends JpaRepository<Term, UUID> {

    List<Term> findByLetterAndStatusOrderByTitleAsc(String letter, TermStatus status);
    List<Term> findByStatusOrderByTitleAsc(TermStatus status);
    Optional<Term> findBySlugAndStatus(String slug, TermStatus status);
    Optional<Term> findByIdAndStatus(UUID id, TermStatus status);

    @EntityGraph(attributePaths = {"relatedTerms", "relatedTerms.relatedTerm", "categories"})
    Optional<Term> findDetailBySlugAndStatus(String slug, TermStatus status);

    @EntityGraph(attributePaths = {"relatedTerms", "relatedTerms.relatedTerm", "categories"})
    Optional<Term> findDetailByIdAndStatus(UUID id, TermStatus status);

    @EntityGraph(attributePaths = {"relatedTerms", "relatedTerms.relatedTerm", "categories"})
    Optional<Term> findDetailById(UUID id);

    List<Term> findBySlugIn(List<String> slugs);

    boolean existsBySlug(String slug);
}
