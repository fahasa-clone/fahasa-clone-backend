package vn.clone.fahasa_backend.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.service.BookService;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
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
    public ResponseEntity<FullBookDTO> getBookById(@PathVariable int id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}
