package com.imperialpedia.api.repository;

import com.imperialpedia.api.dto.termdto.TermListResponse;
import com.imperialpedia.api.entity.term.Term;
import com.imperialpedia.api.entity.term.TermStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermRepository extends JpaRepository<Term, UUID> {

    @Query("""
            select new com.imperialpedia.api.dto.termdto.TermListResponse(t.id, t.slug)
            from Term t
            where t.letter = :letter and t.status = :status
            order by t.title asc
            """)
    List<TermListResponse> findListByLetterAndStatusOrderByTitleAsc(String letter, TermStatus status);

    @Query("""
            select new com.imperialpedia.api.dto.termdto.TermListResponse(t.id, t.slug)
            from Term t
            where t.status = :status
            order by t.title asc
            """)
    List<TermListResponse> findListByStatusOrderByTitleAsc(TermStatus status);


    @EntityGraph(attributePaths = {"relatedTerms", "relatedTerms.relatedTerm"})
    Optional<Term> findDetailBySlugAndStatus(String slug, TermStatus status);

    @EntityGraph(attributePaths = {"relatedTerms", "relatedTerms.relatedTerm"})
    Optional<Term> findDetailByIdAndStatus(UUID id, TermStatus status);

    @EntityGraph(attributePaths = {"relatedTerms", "relatedTerms.relatedTerm"})
    Optional<Term> findDetailById(UUID id);

    List<Term> findBySlugIn(List<String> slugs);

    boolean existsBySlug(String slug);
}
