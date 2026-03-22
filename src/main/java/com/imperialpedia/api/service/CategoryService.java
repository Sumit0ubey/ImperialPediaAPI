package com.imperialpedia.api.service;

import com.imperialpedia.api.dto.termdto.Categories;
import com.imperialpedia.api.entity.term.Category;
import com.imperialpedia.api.exception.ArgumentException;
import com.imperialpedia.api.exception.IntegrityViolationException;
import com.imperialpedia.api.interfaces.CategoryServiceInterface;
import com.imperialpedia.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
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
    @Transactional
    public Categories createCategories(Categories categories) {
        if (categories == null) {
            throw new ArgumentException("Category payload must not be null");
        }

        Category savedCategory = createCategorySafely(categories.getName());
        return modelMapper.map(savedCategory, Categories.class);
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
}
