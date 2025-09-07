package vn.clone.fahasa_backend.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.repository.BookRepository;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public Page<Book> fetchAllBooks(Pageable pageable, String filter) {
        // /search?filter= average(ratings) > 4.5 and brand.name in ['audi', 'land rover'] and (year > 2018 or km < 50000) and color : 'white' and accidents is empty
        Specification<Book> specification = SpecificationsBuilder.createSpecification(filter);
        return bookRepository.findAll(specification, pageable);
    }
}
