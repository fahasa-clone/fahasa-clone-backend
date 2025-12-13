package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.annotation.AdminOnly;
import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;
import vn.clone.fahasa_backend.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @AdminOnly
    public ResponseEntity<Category> createCategory(@RequestBody @Valid Category category) {
        Category result = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<List<CategoryTree>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.buildCategoryTrees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryTree> getCategoryIdList(@PathVariable int id) {
        List<CategoryTree> rootList = categoryService.buildCategoryTrees();
        CategoryTree result = categoryService.searchCategoryTree(rootList, id);
        return ResponseEntity.ok(result);
    }
}
