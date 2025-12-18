package vn.clone.fahasa_backend.service;

import java.util.List;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.request.CreateCategoryDTO;
import vn.clone.fahasa_backend.domain.request.UpdateCategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;

public interface CategoryService {

    CategoryDTO createCategory(CreateCategoryDTO createCategoryDTO);

    CategoryDTO updateCategory(int id, UpdateCategoryDTO updateCategoryDTO);

    void deleteCategory(int id);

    List<CategoryTree> buildCategoryTrees();

    CategoryTree searchCategoryTree(List<CategoryTree> rootList, int categoryId);

    List<Integer> getCategoryIdList(int id);

    Category getCategoryById(int id);
}