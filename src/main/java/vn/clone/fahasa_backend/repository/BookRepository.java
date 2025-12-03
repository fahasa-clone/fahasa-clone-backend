package vn.clone.fahasa_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
}
