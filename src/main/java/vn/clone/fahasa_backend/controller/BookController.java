package vn.clone.fahasa_backend.controller;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.service.BookService;

@RestController
@RequestMapping("/api/books")
@AllArgsConstructor
public class BookController {
    
    private final BookService bookService;

    @GetMapping
    public ResponseEntity<PageResponse<List<BookDTO>>> getAllBooks(Pageable pageable,
                                                                   @RequestParam(value = "filter", defaultValue = "") String filter) {
        return ResponseEntity.ok(bookService.fetchAllBooks(pageable, filter));
    }
}
