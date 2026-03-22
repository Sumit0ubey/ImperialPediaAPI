package com.imperialpedia.api.interfaces;

import com.imperialpedia.api.dto.termdto.Categories;
import com.imperialpedia.api.entity.term.Category;

import java.util.List;

public interface CategoryServiceInterface {

    List<Categories> getAllCategories();
    Categories createCategories(Categories categories);
    Category findOrCreateCategoryByName(String categoryName);
    Category createCategorySafely(String categoryName);
}
