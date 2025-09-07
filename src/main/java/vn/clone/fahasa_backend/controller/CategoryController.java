package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.Category;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;
import vn.clone.fahasa_backend.service.CategoryService;

@RestController
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(@Qualifier("categoryServiceImpl") CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody @Valid Category category) {
        Category result = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryTree>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.buildCategoryTrees());
    }

    @GetMapping("/categories/list/{id}")
    public ResponseEntity<List<Integer>> getCategoryIdList(@PathVariable int id) {
        return ResponseEntity.ok(categoryService.getCategoryIdList(id));
    }
}
