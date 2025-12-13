package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.annotation.AdminOnly;
import vn.clone.fahasa_backend.domain.request.CreateBookRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookImagesRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookRequest;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.service.BookService;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Validated
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<PageResponse<List<BookDTO>>> getAllBooks(Pageable pageable,
                                                                   @RequestParam(value = "filter", defaultValue = "") String filter) {
        Page<BookDTO> bookPage = bookService.fetchAllBooks(pageable, filter);
        PageResponse<List<BookDTO>> pageResponse = new PageResponse<>(pageable.getPageNumber() + 1, pageable.getPageSize(),
                                                                      bookPage.getTotalPages(), bookPage.getContent());
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FullBookDTO> getBookById(@PathVariable @Min(1) int id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AdminOnly
    public ResponseEntity<FullBookDTO> createBook(@Valid CreateBookRequest request) {
        FullBookDTO newBook = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(newBook);
    }

    @PutMapping(value = "/{id}")
    @AdminOnly
    public ResponseEntity<FullBookDTO> updateBook(@PathVariable @Min(1) int id,
                                                  @Valid @RequestBody UpdateBookRequest request) {
        FullBookDTO updatedBook = bookService.updateBook(id, request);
        return ResponseEntity.ok(updatedBook);
    }

    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AdminOnly
    public ResponseEntity<FullBookDTO> updateBookImages(@PathVariable @Min(1) int id,
                                                        @Valid UpdateBookImagesRequest request) {
        FullBookDTO updatedBook = bookService.updateBookImages(id, request);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteById(@PathVariable @Min(1) int id) {
        bookService.deleteById(id);
        return ResponseEntity.ok()
                             .build();
    }

    @GetMapping("/newest-arrival")
    public ResponseEntity<PageResponse<List<BookDTO>>> getNewestArrivalBooks(@RequestParam int page,
                                                                             @RequestParam int size) {
        Page<BookDTO> bookPage = bookService.fetchNewestArrivalBooks(page, size);
        PageResponse<List<BookDTO>> pageResponse = new PageResponse<>(page, size,
                                                                      bookPage.getTotalPages(), bookPage.getContent());
        return ResponseEntity.ok(pageResponse);
    }
}
