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
import vn.clone.fahasa_backend.domain.Author;
import vn.clone.fahasa_backend.domain.request.AuthorRequestDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.service.AuthorService;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @AdminOnly
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody AuthorRequestDTO authorRequestDTO) {
        Author createdAuthor = authorService.createAuthor(authorRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(createdAuthor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Integer id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<List<Author>>> getAllAuthors(Pageable pageable) {
        Page<Author> authorPage = authorService.getAllAuthors(pageable);
        return ResponseEntity.ok(new PageResponse<>(authorPage.getNumber() + 1, authorPage.getSize(),
                                                    authorPage.getTotalPages(), authorPage.getContent()));
    }

    @PutMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Author> updateAuthor(@PathVariable Integer id,
                                               @Valid @RequestBody AuthorRequestDTO requestDTO) {
        Author updatedAuthor = authorService.updateAuthor(id, requestDTO);
        return ResponseEntity.ok(updatedAuthor);
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteAuthor(@PathVariable Integer id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent()
                             .build();
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<Author> getAuthorByName(@RequestParam String name) {
        return authorService.getAuthorByName(name)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }
}
