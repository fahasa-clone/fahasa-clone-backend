package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.annotation.AdminOnly;
import vn.clone.fahasa_backend.domain.Spec;
import vn.clone.fahasa_backend.domain.request.SpecRequestDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.service.SpecService;

@RestController
@RequestMapping("/api/specifications")
@RequiredArgsConstructor
@Validated
public class SpecController {

    private final SpecService specService;

    @PostMapping
    @AdminOnly
    public ResponseEntity<Spec> createSpec(@Valid @RequestBody SpecRequestDTO request) {
        Spec createdSpec = specService.createSpec(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(createdSpec);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Spec> getSpecById(@PathVariable @Min(1) Integer id) {
        return ResponseEntity.ok(specService.getSpecById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<List<Spec>>> getAllSpecs(Pageable pageable) {
        Page<Spec> specPage = specService.getAllSpecs(pageable);
        return ResponseEntity.ok(new PageResponse<>(specPage.getNumber() + 1, specPage.getSize(),
                                                    specPage.getTotalPages(), specPage.getContent()));
    }

    @PutMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Spec> updateSpec(@PathVariable @Min(1) Integer id,
                                           @Valid @RequestBody SpecRequestDTO request) {
        Spec updatedSpec = specService.updateSpec(id, request);
        return ResponseEntity.ok(updatedSpec);
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteSpec(@PathVariable @Min(1) Integer id) {
        specService.deleteSpec(id);
        return ResponseEntity.noContent()
                             .build();
    }
}
