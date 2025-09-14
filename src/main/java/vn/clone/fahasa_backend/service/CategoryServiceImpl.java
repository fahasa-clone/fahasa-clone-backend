package vn.clone.fahasa_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new BadRequestException("Category name already exists");
        }

        if (category.getParent() != null) {
            Optional<Category> parentOptional = categoryRepository.findById(category.getParent().getId());
            if (parentOptional.isEmpty()) {
                throw new BadRequestException("Parent category not found");
            }
            category.setParent(parentOptional.get());
        }

        return categoryRepository.save(category);
    }

    public List<CategoryTree> buildCategoryTrees() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryTree> rootList = categories.stream()
                                                .filter(c -> c.getParent() == null)
                                                .map(c -> new CategoryTree(c.getId(), c.getName(), null))
                                                .toList();
        rootList.forEach(root -> root.setChildren(getChildren(categories, root.getId())));
        return rootList;
    }

    public List<CategoryTree> getChildren(List<Category> categories, int parent_id) {
        List<CategoryTree> children = categories.stream()
                                                .filter(c -> c.getParent() != null && c.getParent()
                                                                                       .getId() == parent_id)
                                                .map(c -> new CategoryTree(c.getId(), c.getName(), null))
                                                .toList();

        if (children.isEmpty()) {
            return null;
        } else {
            children.forEach(child -> child.setChildren(getChildren(categories, child.getId())));
        }

        return children;
    }

    public CategoryTree searchCategoryTree(List<CategoryTree> rootList, int categoryId) {
        if (rootList.isEmpty()) {
            return null;
        }

        for (CategoryTree categoryTree : rootList) {
            if (categoryTree.getId() == categoryId) {
                return categoryTree;
            }
            if (categoryTree.getChildren() != null) {
                CategoryTree result = searchCategoryTree(categoryTree.getChildren(), categoryId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public List<Integer> toListCategory(CategoryTree root) {
        List<Integer> result = new ArrayList<>();
        result.add(root.getId());
        if (root.getChildren() != null) {
            root.getChildren().forEach(c -> result.addAll(toListCategory(c)));
        }
        return result;
    }

    @Override
    public List<Integer> getCategoryIdList(int id) {
        List<CategoryTree> rootList = buildCategoryTrees();
        CategoryTree selectedRoot = searchCategoryTree(rootList, id);
        if (selectedRoot == null) {
            return new ArrayList<>();
        }
        return toListCategory(selectedRoot);
    }
}