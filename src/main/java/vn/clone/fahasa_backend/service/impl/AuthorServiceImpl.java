package vn.clone.fahasa_backend.service.impl;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Author;
import vn.clone.fahasa_backend.domain.request.AuthorRequestDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.AuthorRepository;
import vn.clone.fahasa_backend.service.AuthorService;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional
    public Author createAuthor(AuthorRequestDTO authorRequestDTO) {
        checkAuthorNameExists(authorRequestDTO.getName());

        Author newAuthor = Author.builder()
                                 .name(authorRequestDTO.getName())
                                 .build();

        return authorRepository.save(newAuthor);
    }

    @Override
    @Transactional(readOnly = true)
    public Author getAuthorById(Integer id) {
        return authorRepository.findById(id)
                               .orElseThrow(() -> new EntityNotFoundException("Not found author with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Author> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Author updateAuthor(Integer id, AuthorRequestDTO authorRequestDTO) {
        Author existingAuthor = getAuthorById(id);

        checkAuthorNameExists(authorRequestDTO.getName());

        existingAuthor.setName(authorRequestDTO.getName());

        return authorRepository.save(existingAuthor);
    }

    @Override
    @Transactional
    public void deleteAuthor(Integer id) {
        Author author = getAuthorById(id);

        authorRepository.delete(author);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Author> getAuthorByName(String name) {
        return authorRepository.findByName(name);
    }

    private void checkAuthorNameExists(String name) {
        authorRepository.findByName(name)
                        .ifPresent(author -> {
                            throw new BadRequestException("Author with this name already exists");
                        });
    }
}
