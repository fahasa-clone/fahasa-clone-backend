package vn.clone.fahasa_backend.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;

public interface BookService {
    PageResponse<List<BookDTO>> fetchAllBooks(Pageable pageable, String filter);
}
