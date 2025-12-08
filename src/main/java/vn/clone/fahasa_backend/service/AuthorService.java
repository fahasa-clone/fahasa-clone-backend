package vn.clone.fahasa_backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.clone.fahasa_backend.domain.Author;
import vn.clone.fahasa_backend.domain.request.AuthorRequestDTO;

public interface AuthorService {

    Author createAuthor(AuthorRequestDTO authorRequestDTO);

    Author getAuthorById(Integer id);

    Page<Author> getAllAuthors(Pageable pageable);

    Author updateAuthor(Integer id, AuthorRequestDTO authorRequestDTO);

    void deleteAuthor(Integer id);

    Optional<Author> getAuthorByName(String name);
}
