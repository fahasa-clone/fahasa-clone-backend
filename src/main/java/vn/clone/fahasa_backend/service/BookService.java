package vn.clone.fahasa_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;

public interface BookService {

    Page<BookDTO> fetchAllBooks(Pageable pageable, String filter);

    FullBookDTO getBookById(int id);
}
