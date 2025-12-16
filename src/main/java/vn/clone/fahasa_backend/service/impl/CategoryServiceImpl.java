package vn.clone.fahasa_backend.service.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.request.CreateCategoryDTO;
import vn.clone.fahasa_backend.domain.request.UpdateCategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.CategoryRepository;
import vn.clone.fahasa_backend.service.CategoryService;
import vn.clone.fahasa_backend.util.VietnameseConverter;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // @Bean
    // public CommandLineRunner init() {
    //     return args -> {
    //         List<Category> categoryList = categoryRepository.findAll();
    //         System.out.println("=============================== START =======================================");
    //         categoryList.forEach(category -> {
    //             category.setName(VietnameseConverter.normalizeName(category.getName()));
    //             category.setSlug(VietnameseConverter.convertNameToSlug(category.getName()));
    //         });
    //         categoryRepository.saveAll(categoryList);
    //         System.out.println("================================ END ======================================");
    //     };
    // }

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryDTO createCategoryDTO) {
        validateCategoryNameIsUnique(createCategoryDTO.getName());

        Category category = Category.builder()
                                    .name(createCategoryDTO.getName())
                                    .slug(VietnameseConverter.convertNameToSlug(createCategoryDTO.getName()))
                                    .description(createCategoryDTO.getDescription())
                                    .categoryIcon(createCategoryDTO.getCategoryIcon())
                                    .build();

        if (createCategoryDTO.getParentId() != null) {
            category.setParent(findById(createCategoryDTO.getParentId()));
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(int id, UpdateCategoryDTO updateCategoryDTO) {
        Category category = findById(id);

        if (!category.getName().equals(updateCategoryDTO.getName())) {
            validateCategoryNameIsUnique(updateCategoryDTO.getName());
            category.setName(updateCategoryDTO.getName());
            category.setSlug(VietnameseConverter.convertNameToSlug(updateCategoryDTO.getName()));
        }

        category.setDescription(updateCategoryDTO.getDescription());
        category.setCategoryIcon(updateCategoryDTO.getCategoryIcon());

        if (updateCategoryDTO.getParentId() != null) {
            if (updateCategoryDTO.getParentId() == id) {
                throw new BadRequestException("Parent category cannot be itself!");
            }
            category.setParent(findById(updateCategoryDTO.getParentId()));
        } else {
            category.setParent(null);
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(int id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    public List<CategoryTree> buildCategoryTrees() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryTree> rootList = categories.stream()
                                                .filter(c -> c.getParent() == null)
                                                .map(c -> CategoryTree.builder()
                                                                      .id(c.getId())
                                                                      .name(c.getName())
                                                                      .categoryIcon(c.getCategoryIcon())
                                                                      .slug(c.getSlug())
                                                                      .build())
                                                .toList();
        rootList.forEach(root -> root.setChildren(getChildren(categories, root.getId())));
        return rootList;
    }

    public List<CategoryTree> getChildren(List<Category> categories, int parent_id) {
        List<CategoryTree> children = categories.stream()
                                                .filter(c -> c.getParent() != null && c.getParent()
                                                                                       .getId() == parent_id)
                                                .map(c -> CategoryTree.builder()
                                                                      .id(c.getId())
                                                                      .name(c.getName())
                                                                      .categoryIcon(c.getCategoryIcon())
                                                                      .slug(c.getSlug())
                                                                      .build())
                                                .toList();

        if (children.isEmpty()) {
            return null;
        }
        children.forEach(child -> child.setChildren(getChildren(categories, child.getId())));
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

    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id)
                                 .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    private void validateCategoryNameIsUnique(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new BadRequestException("Category name already exists");
        }
    }

    private Category findById(int id) {
        return categoryRepository.findById(id)
                                 .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    private CategoryDTO convertToCategoryDTO(Category category) {
        CategoryDTO categoryDTO = CategoryDTO.builder()
                                             .id(category.getId())
                                             .name(category.getName())
                                             .slug(category.getSlug())
                                             .description(category.getDescription())
                                             .build();

        Category parent = category.getParent();
        if (parent != null) {
            categoryDTO.setParentCategory(CategoryDTO.ParentCategoryDTO.builder()
                                                                       .id(parent.getId())
                                                                       .name(parent.getName())
                                                                       .build());
        }

        return categoryDTO;
    }
}