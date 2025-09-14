package vn.clone.fahasa_backend.service;

import java.util.List;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;

public interface CategoryService {
    public Category createCategory(Category category);

    public List<CategoryTree> buildCategoryTrees();

    public CategoryTree searchCategoryTree(List<CategoryTree> rootList, int categoryId);

    public List<Integer> getCategoryIdList(int id);
}