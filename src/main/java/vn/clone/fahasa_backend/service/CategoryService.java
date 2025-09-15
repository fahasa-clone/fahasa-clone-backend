package vn.clone.fahasa_backend.service;

import java.util.List;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;

public interface CategoryService {
    Category createCategory(Category category);

    List<CategoryTree> buildCategoryTrees();

    CategoryTree searchCategoryTree(List<CategoryTree> rootList, int categoryId);

    List<Integer> getCategoryIdList(int id);
}