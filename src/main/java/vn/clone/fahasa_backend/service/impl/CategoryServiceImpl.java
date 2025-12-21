package vn.clone.fahasa_backend.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.BookSpec;
import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.request.CreateCategoryDTO;
import vn.clone.fahasa_backend.domain.request.UpdateCategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryBranch;
import vn.clone.fahasa_backend.domain.response.category.CategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;
import vn.clone.fahasa_backend.domain.response.category.GetCategoryPageDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.CategoryRepository;
import vn.clone.fahasa_backend.service.CategoryService;
import vn.clone.fahasa_backend.util.VietnameseConverter;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final BookRepository bookRepository;

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

        // Set parent Category
        if (createCategoryDTO.getParentId() != null) {
            category.setParent(findById(createCategoryDTO.getParentId()));
        }

        // === Save to the database ===
        Category savedCategory = categoryRepository.save(category);

        // === Convert to DTO ===
        return convertToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(int id, UpdateCategoryDTO updateCategoryDTO) {
        Category category = findById(id);

        // === Update Category name ===
        if (!category.getName().equals(updateCategoryDTO.getName())) {
            validateCategoryNameIsUnique(updateCategoryDTO.getName());
            category.setName(updateCategoryDTO.getName());
            category.setSlug(VietnameseConverter.convertNameToSlug(updateCategoryDTO.getName()));
        }

        category.setDescription(updateCategoryDTO.getDescription());
        category.setCategoryIcon(updateCategoryDTO.getCategoryIcon());

        // === Update parent Category ===
        if (updateCategoryDTO.getParentId() != null) {
            if (updateCategoryDTO.getParentId() == id) {
                throw new BadRequestException("Parent category cannot be itself!");
            }
            category.setParent(findById(updateCategoryDTO.getParentId()));
        } else {
            category.setParent(null);
        }

        // === Save to the database ===
        Category savedCategory = categoryRepository.save(category);

        // === Convert to DTO ===
        return convertToCategoryDTO(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(int id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTree> buildCategoryTree() {
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

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public GetCategoryPageDTO getCategoryBranchById(int id) {
        Category category = findById(id);

        // Build the category branch
        List<CategoryTree> rootList = buildCategoryTree();
        CategoryBranch selectedBranch = buildCategoryBranch(rootList, id);

        // Get the deepest category ids under this category
        List<Integer> categoryIds = listDeepestCategories(selectedBranch);

        // Get all Book IDs that reference this category
        List<Integer> bookIds = bookRepository.findBookIdsByCategoryIds(categoryIds);

        // Get all BookSpec values for these books and specs
        List<BookSpec> bookSpecs = bookRepository.findBookSpecsByBookIds(bookIds);

        // Group BookSpec values by Spec ID
        Map<Integer, Set<String>> specValuesMap = bookSpecs.stream()
                                                           .collect(Collectors.groupingBy(
                                                                   bs -> bs.getSpec().getId(),
                                                                   Collectors.mapping(BookSpec::getValue, Collectors.toSet())
                                                           ));

        // Build SpecDTO list - remove duplicates by collecting unique specs
        List<GetCategoryPageDTO.SpecDTO> specDTOs = bookSpecs.stream()
                                                             .map(BookSpec::getSpec)
                                                             .distinct()
                                                             .map(spec -> GetCategoryPageDTO.SpecDTO.builder()
                                                                                                    .id(spec.getId())
                                                                                                    .name(spec.getName())
                                                                                                    .values(specValuesMap.getOrDefault(spec.getId(), new HashSet<>()))
                                                                                                    .build())
                                                             .toList();

        return GetCategoryPageDTO.builder()
                                 .id(category.getId())
                                 .name(category.getName())
                                 .categoryBranch(selectedBranch)
                                 .specs(specDTOs)
                                 .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getCategoryIdList(int id) {
        List<CategoryTree> rootList = buildCategoryTree();
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
                                             .categoryIcon(category.getCategoryIcon())
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

    private List<CategoryTree> getChildren(List<Category> categories, int parent_id) {
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

    private CategoryBranch buildCategoryBranch(List<CategoryTree> rootList, int categoryId) {
        if (rootList == null || rootList.isEmpty()) {
            return null;
        }

        for (CategoryTree root : rootList) {
            List<CategoryTree> children = root.getChildren();

            if (root.getId() == categoryId) {
                if (children != null && !children.isEmpty()) {
                    List<CategoryBranch> childrenWithoutGrandchildren = children.stream()
                                                                                .map(child -> CategoryBranch.builder()
                                                                                                            .id(child.getId())
                                                                                                            .name(child.getName())
                                                                                                            .slug(child.getSlug())
                                                                                                            .build())
                                                                                .toList();
                    return CategoryBranch.builder()
                                         .id(root.getId())
                                         .name(root.getName())
                                         .slug(root.getSlug())
                                         .children(childrenWithoutGrandchildren)
                                         .build();
                }
                return CategoryBranch.builder()
                                     .isTerminationPoint(true)
                                     .build();
            }

            if (children != null && !children.isEmpty()) {
                CategoryBranch result = buildCategoryBranch(children, categoryId);

                if (result != null) {
                    boolean isTerminationPoint = result.isTerminationPoint();

                    if (isTerminationPoint) {
                        List<CategoryBranch> childrenWithoutGrandchildren = children.stream()
                                                                                    .map(c -> CategoryBranch.builder()
                                                                                                            .id(c.getId())
                                                                                                            .name(c.getName())
                                                                                                            .slug(c.getSlug())
                                                                                                            .isTerminationPoint(c.getId() == categoryId)
                                                                                                            .build())
                                                                                    .toList();
                        return CategoryBranch.builder()
                                             .id(root.getId())
                                             .name(root.getName())
                                             .slug(root.getSlug())
                                             .isParentOfTerminationPoint(true)
                                             .children(childrenWithoutGrandchildren)
                                             .build();
                    }

                    return CategoryBranch.builder()
                                         .id(root.getId())
                                         .name(root.getName())
                                         .slug(root.getSlug())
                                         .children(List.of(result))
                                         .build();
                }
            }
        }

        return null;
    }

    private List<Integer> toListCategory(CategoryTree root) {
        List<Integer> result = new ArrayList<>();
        result.add(root.getId());
        if (root.getChildren() != null) {
            root.getChildren()
                .forEach(c -> result.addAll(toListCategory(c)));
        }
        return result;
    }

    private List<Integer> listDeepestCategories(CategoryBranch root) {
        List<Integer> result = new ArrayList<>();
        List<CategoryBranch> children = root.getChildren();

        if (children != null) {
            if (root.isParentOfTerminationPoint()) {
                result.add(children.stream()
                                   .filter(CategoryBranch::isTerminationPoint)
                                   .findFirst()
                                   .map(CategoryBranch::getId)
                                   .orElse(null));
            } else {
                children.forEach(c -> result.addAll(listDeepestCategories(c)));
            }
        } else {
            result.add(root.getId());
        }

        return result;
    }
}