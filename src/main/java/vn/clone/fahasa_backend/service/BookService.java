package vn.clone.fahasa_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.request.CreateBookRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookImagesRequest;
import vn.clone.fahasa_backend.domain.request.UpdateBookRequest;
import vn.clone.fahasa_backend.domain.response.BookDTO;
import vn.clone.fahasa_backend.domain.response.FullBookDTO;

public interface BookService {

    Page<BookDTO> fetchAllBooks(Pageable pageable, String filter);

    FullBookDTO getBookById(int id);

    FullBookDTO createBook(CreateBookRequest request);

    FullBookDTO updateBook(int id, UpdateBookRequest request);

    void deleteById(int id);

    FullBookDTO updateBookImages(int bookId, UpdateBookImagesRequest request);

    Book findBookOrThrow(int id);
}
