package vn.clone.fahasa_backend.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.BookSpec;
import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.request.CreateCategoryDTO;
import vn.clone.fahasa_backend.domain.request.UpdateCategoryDTO;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.domain.response.category.*;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.CategoryRepository;
import vn.clone.fahasa_backend.service.BookService;
import vn.clone.fahasa_backend.service.CategoryService;
import vn.clone.fahasa_backend.util.VietnameseConverter;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final BookRepository bookRepository;

    private BookService bookService;

    @Autowired
    @Lazy
    public void setBookService(BookService bookService) {
        this.bookService = bookService;
    }

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
        Category savedCategory;
        while (true) {
            try {
                savedCategory = categoryRepository.save(category);
                break;
            } catch (DataIntegrityViolationException ignored) {
                category.setSlug(category.getSlug() + "-" + category.getId());
            }
        }

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
                                                                      .path("/" + c.getSlug())
                                                                      .build())
                                                .toList();
        rootList.forEach(root -> root.setChildren(getChildren(categories, root)));
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

    private CategoryTree findCategoryInCategoryTree(List<CategoryTree> rootList, String slug) {
        return rootList.stream()
                       .filter(c -> slug.equals(c.getSlug()))
                       .findFirst()
                       .orElse(null);
    }

    @Override
    public CategoryPageDTO getCategoryBookPageData(String path, Pageable pageable, String filter) {
        // Build the category tree
        List<CategoryTree> rootList = buildCategoryTree();

        // Handle path segments
        String[] pathSegments = path.split("/");
        String currentCategorySlug = pathSegments[0];

        // Initialize breadcrumbs with home and all-categories
        List<BreadcrumbDTO> categoryBreadcrumbs = new ArrayList<>();
        categoryBreadcrumbs.add(BreadcrumbDTO.builder()
                                             .name("Trang chủ")
                                             .path("/")
                                             .build());
        categoryBreadcrumbs.add(BreadcrumbDTO.builder()
                                             .name("Tất cả nhóm sản phẩm")
                                             .path("/tat-ca-nhom-san-pham")
                                             .build());

        // Initialize parent categories
        List<BreadcrumbDTO> parentCategories = null;

        // Initialize current category node
        CategoryNodeDTO currentCategoryNode;

        // Initialize child categories
        List<CategoryNodeDTO> childCategories;

        if (pathSegments.length == 1 && currentCategorySlug.equals("tat-ca-nhom-san-pham")) {
            currentCategoryNode = CategoryNodeDTO.builder()
                                                 .name(categoryBreadcrumbs.get(1).getName())
                                                 .path(categoryBreadcrumbs.get(1).getPath())
                                                 .children(null)
                                                 .build();
            childCategories = convertTreeNodesToCategoryNodes(rootList);
        } else {
            String categoryIds = "";

            CategoryTree currentCategoryTreeNode = findCategoryInCategoryTree(rootList, currentCategorySlug);
            if (currentCategoryTreeNode == null) {
                throw new BadRequestException("Category not found");
            }

            categoryBreadcrumbs.add(BreadcrumbDTO.builder()
                                                 .id(currentCategoryTreeNode.getId())
                                                 .name(currentCategoryTreeNode.getName())
                                                 .path(currentCategoryTreeNode.getPath())
                                                 .build());

            // Initialize current category node
            currentCategoryNode = CategoryNodeDTO.builder()
                                                 .id(currentCategoryTreeNode.getId())
                                                 .name(currentCategoryTreeNode.getName())
                                                 .path(currentCategoryTreeNode.getPath())
                                                 .children(null)
                                                 .build();

            parentCategories = new ArrayList<>();
            parentCategories.add(categoryBreadcrumbs.get(1));

            childCategories = convertTreeNodesToCategoryNodes(currentCategoryTreeNode.getChildren());

            List<CategoryTree> currentCategoryTreeNodeChildren = currentCategoryTreeNode.getChildren();

            if (pathSegments.length == 1) {
                categoryIds = listDeepestCategories(currentCategoryTreeNode).stream()
                                                                            .map(String::valueOf)
                                                                            .collect(Collectors.joining(","));
            }

            // Process category path segments
            for (int i = 1; i < pathSegments.length; i++) {
                currentCategorySlug = pathSegments[i];
                CategoryTree foundNode = findCategoryInCategoryTree(currentCategoryTreeNodeChildren, currentCategorySlug);

                // Handle not found or invalid path
                if (foundNode == null || (foundNode.getChildren() == null && i < pathSegments.length - 1)) {
                    throw new BadRequestException("Category not found");
                }

                categoryBreadcrumbs.add(BreadcrumbDTO.builder()
                                                     .id(foundNode.getId())
                                                     .name(foundNode.getName())
                                                     .path(foundNode.getPath())
                                                     .build());

                // Case 1: Reached a leaf category before the end
                if (foundNode.getChildren() == null) {
                    parentCategories = categoryBreadcrumbs.subList(1, categoryBreadcrumbs.size() - 2);
                    currentCategoryNode.setIsParentOfActiveNode(true);

                    final String slugForLambda = currentCategorySlug;
                    childCategories = currentCategoryTreeNodeChildren.stream()
                                                                     .map(c -> CategoryNodeDTO.builder()
                                                                                              .id(c.getId())
                                                                                              .name(c.getName())
                                                                                              .path(c.getPath())
                                                                                              .isActive(slugForLambda.equals(c.getSlug()))
                                                                                              .build())
                                                                     .toList();

                    categoryIds = foundNode.getId()
                                           .toString();
                    break;
                }

                currentCategoryNode = CategoryNodeDTO.builder()
                                                     .id(foundNode.getId())
                                                     .name(foundNode.getName())
                                                     .path(foundNode.getPath())
                                                     .children(null)
                                                     .build();

                // Case 2: Reached the final category in the path
                if (i == pathSegments.length - 1) {
                    parentCategories = categoryBreadcrumbs.subList(1, categoryBreadcrumbs.size() - 1);
                    currentCategoryNode.setIsActive(true);
                    childCategories = convertTreeNodesToCategoryNodes(foundNode.getChildren());

                    categoryIds = listDeepestCategories(foundNode).stream()
                                                                  .map(String::valueOf)
                                                                  .collect(Collectors.joining(","));
                    break;
                }

                currentCategoryTreeNodeChildren = foundNode.getChildren();
            }

            String categoryFilter = String.format("category.id in [%s]", categoryIds);
            if (filter != null) {
                filter += " and " + categoryFilter;
            } else {
                filter = categoryFilter;
            }
        }

        // Fetch paginated books filtered by category IDs (and additional filters if provided)
        Page<BookDTO> bookDTOPage = bookService.fetchAllBooks(pageable, filter, null, null);

        return CategoryPageDTO.builder()
                              .categoryBreadcrumbs(categoryBreadcrumbs)
                              .parentCategories(parentCategories)
                              .currentCategoryNode(currentCategoryNode)
                              .childCategories(childCategories)
                              .pageResponse(PageResponse.<List<BookDTO>>builder()
                                                        .pageNumber(bookDTOPage.getNumber() + 1)
                                                        .pageSize(bookDTOPage.getSize())
                                                        .totalPages(bookDTOPage.getTotalPages())
                                                        .items(bookDTOPage.getContent())
                                                        .build())
                              .build();
    }

    private List<CategoryNodeDTO> convertTreeNodesToCategoryNodes(List<CategoryTree> treeNodes) {
        if (treeNodes == null) {
            return null;
        }
        return treeNodes.stream()
                        .map(node -> CategoryNodeDTO.builder()
                                                    .id(node.getId())
                                                    .name(node.getName())
                                                    .path(node.getPath())
                                                    .build())
                        .toList();
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

    private List<CategoryTree> getChildren(List<Category> categories, CategoryTree parent) {
        List<CategoryTree> children = categories.stream()
                                                .filter(c -> c.getParent() != null && Objects.equals(c.getParent()
                                                                                                      .getId(), parent.getId()))
                                                .map(c -> CategoryTree.builder()
                                                                      .id(c.getId())
                                                                      .name(c.getName())
                                                                      .categoryIcon(c.getCategoryIcon())
                                                                      .slug(c.getSlug())
                                                                      .path(parent.getPath() + "/" + c.getSlug())
                                                                      .build())
                                                .toList();

        if (children.isEmpty()) {
            return null;
        }
        children.forEach(child -> child.setChildren(getChildren(categories, child)));
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
                                                                                                            .path(child.getPath())
                                                                                                            .build())
                                                                                .toList();
                    return CategoryBranch.builder()
                                         .id(root.getId())
                                         .name(root.getName())
                                         .path(root.getPath())
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
                                                                                                            .path(c.getPath())
                                                                                                            .isTerminationPoint(c.getId() == categoryId)
                                                                                                            .build())
                                                                                    .toList();
                        return CategoryBranch.builder()
                                             .id(root.getId())
                                             .name(root.getName())
                                             .path(root.getPath())
                                             .isParentOfTerminationPoint(true)
                                             .children(childrenWithoutGrandchildren)
                                             .build();
                    }

                    return CategoryBranch.builder()
                                         .id(root.getId())
                                         .name(root.getName())
                                         .path(root.getPath())
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

    private List<Integer> listDeepestCategories(CategoryTree root) {
        List<Integer> result = new ArrayList<>();
        List<CategoryTree> children = root.getChildren();

        if (children != null) {
            children.forEach(c -> result.addAll(listDeepestCategories(c)));
        } else {
            result.add(root.getId());
        }

        return result;
    }
}