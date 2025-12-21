package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.annotation.AdminOnly;
import vn.clone.fahasa_backend.domain.request.CreateCategoryDTO;
import vn.clone.fahasa_backend.domain.request.UpdateCategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryDTO;
import vn.clone.fahasa_backend.domain.response.category.CategoryTree;
import vn.clone.fahasa_backend.domain.response.category.GetCategoryPageDTO;
import vn.clone.fahasa_backend.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @AdminOnly
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody @Valid CreateCategoryDTO createCategoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(categoryService.createCategory(createCategoryDTO));
    }

    @GetMapping
    public ResponseEntity<List<CategoryTree>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.buildCategoryTree());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetCategoryPageDTO> getCategoryIdList(@PathVariable @Min(1) int id) {
        GetCategoryPageDTO result = categoryService.getCategoryBranchById(id);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    @AdminOnly
    public ResponseEntity<CategoryDTO> updateCategoryById(@PathVariable @Min(1) int id,
                                                          @RequestBody @Valid UpdateCategoryDTO updateCategoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, updateCategoryDTO));
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteCategoryById(@PathVariable @Min(1) int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent()
                             .build();
    }
}
