package com.imperialpedia.api.interfaces;

import com.imperialpedia.api.dto.termdto.Categories;
import com.imperialpedia.api.entity.term.Category;

import java.util.List;
import java.util.Map;

public interface CategoryServiceInterface {

    List<Categories> getAllCategories();
    List<Categories> getCategoriesByLetter(String letter);
    Categories createCategories(Categories categories);
    Categories updateCategory(int id, Categories categories);
    Categories patchCategory(int id, Map<String, Object> request);
    void deleteCategory(int id);
    Category findOrCreateCategoryByName(String categoryName);
    Category createCategorySafely(String categoryName);
}
