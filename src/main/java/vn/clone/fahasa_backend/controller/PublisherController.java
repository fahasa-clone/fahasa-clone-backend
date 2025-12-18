package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.annotation.AdminOnly;
import vn.clone.fahasa_backend.domain.Publisher;
import vn.clone.fahasa_backend.domain.request.PublisherRequestDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.service.PublisherService;

@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    @AdminOnly
    public ResponseEntity<Publisher> createPublisher(@Valid @RequestBody PublisherRequestDTO request) {
        Publisher createdPublisher = publisherService.createPublisher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(createdPublisher);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publisher> getPublisherById(@PathVariable Integer id) {
        return ResponseEntity.ok(publisherService.getPublisherById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<List<Publisher>>> getAllPublishers(Pageable pageable) {
        Page<Publisher> publisherPage = publisherService.getAllPublishers(pageable);
        return ResponseEntity.ok(new PageResponse<>(publisherPage.getNumber() + 1, publisherPage.getSize(),
                                                    publisherPage.getTotalPages(), publisherPage.getContent()));
    }

    @PutMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Publisher> updatePublisher(@PathVariable Integer id,
                                                     @Valid @RequestBody PublisherRequestDTO request) {
        Publisher updatedPublisher = publisherService.updatePublisher(id, request);
        return ResponseEntity.ok(updatedPublisher);
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deletePublisher(@PathVariable Integer id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent()
                             .build();
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<Publisher> getPublisherByName(@RequestParam String name) {
        return publisherService.getPublisherByName(name)
                               .map(ResponseEntity::ok)
                               .orElse(ResponseEntity.notFound().build());
    }
}
