package com.imperialpedia.api.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import java.util.*;

import com.imperialpedia.api.exception.ResourceAlreadyExistsException;
import com.imperialpedia.api.exception.ArgumentException;
import com.imperialpedia.api.exception.ResourceNotFoundException;
import com.imperialpedia.api.interfaces.TermServiceInterface;
import com.imperialpedia.api.dto.termdto.RelatedTermResponse;
import com.imperialpedia.api.dto.termdto.TermDetailResponse;
import com.imperialpedia.api.dto.termdto.TermListResponse;
import com.imperialpedia.api.repository.TermRepository;
import com.imperialpedia.api.util.TermInputUtils;
import com.imperialpedia.api.entity.term.RelatedTerm;
import com.imperialpedia.api.entity.term.TermStatus;
import com.imperialpedia.api.entity.term.Category;
import com.imperialpedia.api.dto.termdto.AddTerm;
import com.imperialpedia.api.entity.term.Term;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService implements TermServiceInterface {

    private final ModelMapper modelMapper;
    private final TermRepository termRepository;
    private final CategoryService categoryService;

    private static final Set<String> PATCHABLE_FIELDS = Set.of(
            "title",
            "slug",
            "content",
            "seoTitle",
            "seoDescription",
            "featuredImageUrl",
            "status",
            "categoryNames",
            "relatedTerms"
    );

    @Override
    public List<TermListResponse> getTermsByLetter(String letter) {
        String normalizedLetter = TermInputUtils.normalizeOptionalLetter(letter);
        if (normalizedLetter == null) {
            throw new ArgumentException("Letter must not be blank");
        }

        List<TermListResponse> terms = termRepository.findListByLetterAndStatusOrderByTitleAsc(
                normalizedLetter,
                TermStatus.PUBLISHED
        );

        if (terms.isEmpty()) {
            throw new ResourceNotFoundException("No terms found for letter: " + letter);
        }

        return terms;
    }

    @Override
    public List<TermListResponse> getArchivedTerms(String letter) {
        return getTermsByStatusAndOptionalLetter(TermStatus.ARCHIVED, letter, "archived");
    }

    @Override
    public List<TermListResponse> getDraftTerms(String letter) {
        return getTermsByStatusAndOptionalLetter(TermStatus.DRAFT, letter, "draft");
    }

    private List<TermListResponse> getTermsByStatusAndOptionalLetter(TermStatus status, String letter, String statusLabel) {
        String normalizedLetter = TermInputUtils.normalizeOptionalLetter(letter);

        List<TermListResponse> terms = normalizedLetter == null
                ? termRepository.findListByStatusOrderByTitleAsc(status)
                : termRepository.findListByLetterAndStatusOrderByTitleAsc(normalizedLetter, status);

        if (terms.isEmpty()) {
            if (normalizedLetter == null) {
                throw new ResourceNotFoundException("No " + statusLabel + " terms found");
            }

            throw new ResourceNotFoundException(
                    "No " + statusLabel + " terms found for letter: " + normalizedLetter
            );
        }

        return terms;
    }


    @Override
    public TermDetailResponse getTermDetailBySlug(String slug) {
        String normalizedSlug = TermInputUtils.normalizeSlug(slug);

        Term term = termRepository.findDetailBySlugAndStatus(normalizedSlug, TermStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        return getTermDetails(term);
    }

    @Override
    public TermDetailResponse getTermDetailById(UUID id) {
        Term term = termRepository.findDetailByIdAndStatus(id, TermStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        return getTermDetails(term);
    }

    @Override
    @Transactional
    public TermDetailResponse addTerm(AddTerm request) {
        normalizeAddTermInput(request);
        ensureSlugAvailable(request.getSlug(), null);

        Term term = modelMapper.map(request, Term.class);
        term.setStatus(request.getStatus() != null ? request.getStatus() : TermStatus.DRAFT);
        term.setCategories(resolveCategoriesByName(request.getCategoryNames()));
        term.setRelatedTerms(buildRelatedLinks(term, resolveRelatedTerms(request)));

        return saveAndBuildDetail(term);
    }

    @Override
    @Transactional
    public TermDetailResponse updateTerm(UUID id, AddTerm request) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        normalizeAddTermInput(request);
        ensureSlugAvailable(request.getSlug(), term.getSlug());

        modelMapper.map(request, term);
        term.setStatus(request.getStatus() != null ? request.getStatus() : TermStatus.DRAFT);
        term.setCategories(resolveCategoriesByName(request.getCategoryNames()));
        term.setRelatedTerms(buildRelatedLinks(term, resolveRelatedTerms(request)));

        return saveAndBuildDetail(term);
    }

    @Override
    @Transactional
    public TermDetailResponse patchTerm(UUID id, Map<String, Object> request) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        if (request == null || request.isEmpty()) {
            return getTermDetails(term);
        }

        validatePatchableFields(request.keySet());

        if (request.containsKey("title")) {
            term.setTitle(TermInputUtils.normalizeRequiredLowercaseString(request.get("title"), "title"));
        }

        if (request.containsKey("slug")) {
            String slug = TermInputUtils.normalizeSlug(TermInputUtils.requireNonBlankString(request.get("slug"), "slug"));
            ensureSlugAvailable(slug, term.getSlug());
            term.setSlug(slug);
        }

        if (request.containsKey("content")) {
            term.setContent(TermInputUtils.normalizeRequiredLowercaseString(request.get("content"), "content"));
        }

        if (request.containsKey("seoTitle")) {
            term.setSeoTitle(TermInputUtils.normalizeNullableLowercaseString(request.get("seoTitle"), "seoTitle"));
        }

        if (request.containsKey("seoDescription")) {
            term.setSeoDescription(TermInputUtils.normalizeNullableLowercaseString(request.get("seoDescription"), "seoDescription"));
        }

        if (request.containsKey("featuredImageUrl")) {
            term.setFeaturedImageUrl(TermInputUtils.normalizeNullableLowercaseString(request.get("featuredImageUrl"), "featuredImageUrl"));
        }

        if (request.containsKey("status")) {
            term.setStatus(TermInputUtils.parseTermStatus(request.get("status")));
        }

        if (request.containsKey("categoryNames")) {
            term.setCategories(resolveCategoriesByName(TermInputUtils.getStringList(request.get("categoryNames"), "categoryNames")));
        }

        if (request.containsKey("relatedTerms")) {
            AddTerm relatedRequest = new AddTerm();
            relatedRequest.setRelatedTerms(TermInputUtils.getStringList(request.get("relatedTerms"), "relatedTerms"));
            term.setRelatedTerms(buildRelatedLinks(term, resolveRelatedTerms(relatedRequest)));
        }

        return saveAndBuildDetail(term);
    }

    @Override
    @Transactional
    public TermDetailResponse publishTerm(UUID id) {
        Term term = termRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        term.setStatus(TermStatus.PUBLISHED);
        return saveAndBuildDetail(term);
    }

    @Override
    @Transactional
    public TermDetailResponse makeDraftTerm(UUID id) {
        Term term = termRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        term.setStatus(TermStatus.DRAFT);
        return saveAndBuildDetail(term);
    }

    @Override
    @Transactional
    public TermDetailResponse makeArchivedTerm(UUID id) {
        Term term = termRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        term.setStatus(TermStatus.ARCHIVED);
        return saveAndBuildDetail(term);
    }

    @Override
    @Transactional
    public void deleteTerm(UUID id) {
        if (id == null) {
            throw new ArgumentException("Term ID must not be null");
        }

        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        termRepository.delete(term);
    }

    private TermDetailResponse saveAndBuildDetail(Term term) {
        Term saved = termRepository.save(term);
        Term refreshed = termRepository.findDetailById(saved.getId())
                .orElse(saved);
        return getTermDetails(refreshed);
    }

    private void normalizeAddTermInput(AddTerm request) {
        if (request == null) {
            throw new ArgumentException("Request payload must not be null");
        }

        if (request.getTitle() == null) {
            throw new ArgumentException("Title is required");
        }
        if (request.getSlug() == null) {
            throw new ArgumentException("Slug is required");
        }
        if (request.getContent() == null) {
            throw new ArgumentException("Content is required");
        }

        request.setTitle(TermInputUtils.normalizeRequiredLowercaseString(request.getTitle(), "title"));
        request.setSlug(TermInputUtils.normalizeSlug(request.getSlug()));
        request.setContent(TermInputUtils.normalizeRequiredLowercaseString(request.getContent(), "content"));
        request.setSeoTitle(TermInputUtils.normalizeNullableLowercaseString(request.getSeoTitle(), "seoTitle"));
        request.setSeoDescription(TermInputUtils.normalizeNullableLowercaseString(request.getSeoDescription(), "seoDescription"));
        request.setFeaturedImageUrl(TermInputUtils.normalizeNullableLowercaseString(request.getFeaturedImageUrl(), "featuredImageUrl"));

        if (request.getCategoryNames() != null) {
            request.setCategoryNames(request.getCategoryNames().stream()
                    .filter(Objects::nonNull)
                    .map(TermInputUtils::normalizeLowercase)
                    .toList());
        }

        if (request.getRelatedTerms() != null) {
            request.setRelatedTerms(request.getRelatedTerms().stream()
                    .filter(Objects::nonNull)
                    .map(TermInputUtils::normalizeLowercase)
                    .toList());
        }
    }

    private TermDetailResponse getTermDetails(Term term) {
        List<RelatedTermResponse> related = term.getRelatedTerms().stream()
                .map(RelatedTerm::getRelatedTerm)
                .filter(Objects::nonNull)
                .map(relatedTerm -> new RelatedTermResponse(
                        relatedTerm.getId(),
                        relatedTerm.getSlug(),
                        relatedTerm.getTitle(),
                        relatedTerm.getFeaturedImageUrl()
                ))
                .sorted(Comparator.comparing(RelatedTermResponse::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<String> categoryNames = term.getCategories().stream()
                .map(Category::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        TermDetailResponse response = modelMapper.map(term, TermDetailResponse.class);
        response.setCategoryNames(categoryNames);
        response.setRelatedTerms(related);
        return response;
    }

    private void ensureSlugAvailable(String candidateSlug, String currentSlug) {
        if (candidateSlug == null || candidateSlug.isBlank()) {
            throw new ArgumentException("Slug is required");
        }

        String normalizedCandidate = TermInputUtils.normalizeSlug(candidateSlug);
        String normalizedCurrent = currentSlug == null ? null : TermInputUtils.normalizeLowercase(currentSlug);

        if (!Objects.equals(normalizedCurrent, normalizedCandidate) && termRepository.existsBySlug(normalizedCandidate)) {
            throw new ResourceAlreadyExistsException("Slug already exists: " + candidateSlug);
        }
    }

    private List<RelatedTerm> buildRelatedLinks(Term term, List<Term> relatedTargets) {
        if (relatedTargets == null || relatedTargets.isEmpty()) {
            return new ArrayList<>();
        }

        for (Term relatedTarget : relatedTargets) {
            if (term != null && term.getId() != null && Objects.equals(term.getId(), relatedTarget.getId())) {
                throw new ArgumentException("A term cannot reference itself as related");
            }
        }

        return relatedTargets.stream().map(target -> {
            RelatedTerm relatedTerm = new RelatedTerm();
            relatedTerm.setTerm(term);
            relatedTerm.setRelatedTerm(target);
            return relatedTerm;
        }).toList();
    }

    private void validatePatchableFields(Set<String> requestKeys) {
        List<String> unsupportedFields = requestKeys.stream()
                .filter(field -> !PATCHABLE_FIELDS.contains(field))
                .sorted()
                .toList();

        if (!unsupportedFields.isEmpty()) {
            throw new ArgumentException("Unsupported patch fields: " + String.join(", ", unsupportedFields));
        }
    }

    private List<Term> resolveRelatedTerms(AddTerm request) {
        List<String> requestedSlugs = TermInputUtils.normalizeRelatedTermSlugs(request.getRelatedTerms());
        if (requestedSlugs.isEmpty()) {
            return new ArrayList<>();
        }

        List<Term> relatedTargets = termRepository.findBySlugIn(requestedSlugs);
        Set<String> foundSlugs = relatedTargets.stream()
                .map(Term::getSlug)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        List<String> missingSlugs = requestedSlugs.stream()
                .filter(slug -> !foundSlugs.contains(slug))
                .toList();

        if (!missingSlugs.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Related terms not found for: " + String.join(", ", missingSlugs)
            );
        }

        return relatedTargets;
    }

    private List<Category> resolveCategoriesByName(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalizedNames = new LinkedHashSet<>();
        for (String categoryName : categoryNames) {
            if (categoryName == null) {
                continue;
            }

            String normalized = TermInputUtils.normalizeLowercase(categoryName);
            if (!normalized.isBlank()) {
                normalizedNames.add(normalized.replaceAll("\\s+", " "));
            }
        }

        List<Category> categories = new ArrayList<>();
        for (String categoryName : normalizedNames) {
            categories.add(categoryService.findOrCreateCategoryByName(categoryName));
        }

        return categories;
    }
}
