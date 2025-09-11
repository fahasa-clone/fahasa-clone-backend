package vn.clone.fahasa_backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.service.BookService;

@RestController
@RequestMapping("/books")
@AllArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(Pageable pageable,
                                                  @RequestParam("filter") String filter) {
        return ResponseEntity.ok(bookService.fetchAllBooks(pageable, filter));
    }

}
