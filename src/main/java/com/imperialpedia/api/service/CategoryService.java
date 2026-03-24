package com.imperialpedia.api.service;

import com.imperialpedia.api.dto.termdto.Categories;
import com.imperialpedia.api.entity.term.Category;
import com.imperialpedia.api.exception.ArgumentException;
import com.imperialpedia.api.exception.IntegrityViolationException;
import com.imperialpedia.api.exception.ResourceAlreadyExistsException;
import com.imperialpedia.api.exception.ResourceNotFoundException;
import com.imperialpedia.api.interfaces.CategoryServiceInterface;
import com.imperialpedia.api.repository.CategoryRepository;
import com.imperialpedia.api.util.TermInputUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService implements CategoryServiceInterface {

    private static final int MAX_SLUG_LENGTH = 120;
    private static final String DEFAULT_CATEGORY_SLUG = "category";
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public List<Categories> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        return categories.stream()
                .map(category -> modelMapper.map(category, Categories.class))
                .toList();
    }

    @Override
    public List<Categories> getCategoriesByLetter(String letter) {
        String normalizedLetter = TermInputUtils.normalizeOptionalLetter(letter);
        if (normalizedLetter == null) {
            throw new ArgumentException("Letter must be a single alphabetic character");
        }

        List<Category> categories = categoryRepository.findByNameStartingWithIgnoreCaseOrderByNameAsc(normalizedLetter);
        if (categories.isEmpty()) {
            throw new ResourceNotFoundException("No categories found for letter: " + letter);
        }

        return categories.stream()
                .map(category -> modelMapper.map(category, Categories.class))
                .toList();
    }

    @Override
    @Transactional
    public Categories createCategories(Categories categories) {
        if (categories == null) {
            throw new ArgumentException("Category payload must not be null");
        }

        Category savedCategory = createCategorySafely(categories.getName());
        return modelMapper.map(savedCategory, Categories.class);
    }

    @Override
    @Transactional
    public Categories updateCategory(int id, Categories categories) {
        validateCategoryId(id);
        if (categories == null) {
            throw new ArgumentException("Category payload must not be null");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String normalizedName = normalizeCategoryName(categories.getName());
        validateNameLength(normalizedName);
        ensureCategoryNameAvailable(normalizedName, id);

        category.setName(normalizedName);
        category.setSlug(generateUniqueCategorySlugForUpdate(normalizedName, id));

        Category saved = categoryRepository.save(category);
        return modelMapper.map(saved, Categories.class);
    }

    @Override
    @Transactional
    public Categories patchCategory(int id, Map<String, Object> request) {
        validateCategoryId(id);
        if (request == null || request.isEmpty()) {
            throw new ArgumentException("Patch payload must not be empty");
        }

        Set<String> allowedFields = Set.of("name");
        Set<String> unsupported = request.keySet().stream()
                .filter(field -> !allowedFields.contains(field))
                .collect(java.util.stream.Collectors.toSet());

        if (!unsupported.isEmpty()) {
            throw new ArgumentException("Unsupported patch fields: " + unsupported);
        }

        Object rawName = request.get("name");
        if (!(rawName instanceof String name)) {
            throw new ArgumentException("name must be a string");
        }

        return updateCategory(id, new Categories(name));
    }

    @Override
    @Transactional
    public void deleteCategory(int id) {
        validateCategoryId(id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IntegrityViolationException("Category cannot be deleted because it is linked to existing terms");
        }
    }

    @Override
    public Category findOrCreateCategoryByName(String categoryName) {
        return createCategorySafely(categoryName);
    }

    @Override
    @Transactional
    public Category createCategorySafely(String categoryName) {
        String normalizedName = normalizeCategoryName(categoryName);

        return categoryRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> saveNewCategory(normalizedName));
    }

    private Category saveNewCategory(String normalizedName) {
        Category category = new Category();
        category.setName(normalizedName);
        category.setSlug(generateUniqueCategorySlug(normalizedName));

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            return categoryRepository.findByNameIgnoreCase(normalizedName)
                    .orElseThrow(() -> new IntegrityViolationException("Failed to create category: " + normalizedName));
        }
    }

    private String generateUniqueCategorySlug(String categoryName) {
        String baseSlug = slugifyNormalizedName(categoryName);
        String candidate = baseSlug;
        int suffix = 2;

        while (categoryRepository.existsBySlug(candidate)) {
            String suffixToken = "-" + suffix;
            int maxBaseLength = Math.max(1, MAX_SLUG_LENGTH - suffixToken.length());
            String trimmedBase = baseSlug.length() > maxBaseLength
                    ? baseSlug.substring(0, maxBaseLength)
                    : baseSlug;
            candidate = trimmedBase + suffixToken;
            suffix++;
        }

        return candidate;
    }

    private String slugifyNormalizedName(String normalizedName) {
        String slug = NON_ALNUM.matcher(normalizedName).replaceAll("-");
        slug = EDGE_DASHES.matcher(slug).replaceAll("");

        if (slug.isEmpty()) {
            slug = DEFAULT_CATEGORY_SLUG;
        }

        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH).replaceAll("-+$", "");
        }

        return slug;
    }

    private String normalizeCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new ArgumentException("Category name must not be blank");
        }

        String normalized = MULTI_SPACE.matcher(categoryName.trim()).replaceAll(" ");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private void validateNameLength(String normalizedName) {
        if (normalizedName.length() > 255) {
            throw new ArgumentException("name must be at most 255 characters");
        }
    }

    private void validateCategoryId(int id) {
        if (id <= 0) {
            throw new ArgumentException("Category id must be a positive integer");
        }
    }

    private void ensureCategoryNameAvailable(String normalizedName, int currentCategoryId) {
        categoryRepository.findByNameIgnoreCase(normalizedName)
                .ifPresent(existing -> {
                    if (existing.getId() != currentCategoryId) {
                        throw new ResourceAlreadyExistsException("Category with this name already exists");
                    }
                });
    }

    private String generateUniqueCategorySlugForUpdate(String categoryName, int categoryId) {
        String baseSlug = slugifyNormalizedName(categoryName);
        String candidate = baseSlug;
        int suffix = 2;

        while (categoryRepository.existsBySlugAndIdNot(candidate, categoryId)) {
            String suffixToken = "-" + suffix;
            int maxBaseLength = Math.max(1, MAX_SLUG_LENGTH - suffixToken.length());
            String trimmedBase = baseSlug.length() > maxBaseLength
                    ? baseSlug.substring(0, maxBaseLength)
                    : baseSlug;
            candidate = trimmedBase + suffixToken;
            suffix++;
        }

        return candidate;
    }
}
