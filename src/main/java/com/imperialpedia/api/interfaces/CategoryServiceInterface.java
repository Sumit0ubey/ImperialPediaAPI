package com.imperialpedia.api.interfaces;

import com.imperialpedia.api.dto.termdto.AddCategories;
import com.imperialpedia.api.dto.termdto.CategoryDetailResponse;
import com.imperialpedia.api.entity.term.Category;

import java.util.List;
import java.util.Map;

public interface CategoryServiceInterface {

    List<CategoryDetailResponse> getAllCategories();
    List<CategoryDetailResponse> getCategoriesByLetter(String letter);
    AddCategories createCategories(AddCategories addCategories);
    AddCategories updateCategory(int id, AddCategories addCategories);
    AddCategories patchCategory(int id, Map<String, Object> request);
    void deleteCategory(int id);
    Category findOrCreateCategoryByName(String categoryName);
    Category createCategorySafely(String categoryName);
}
