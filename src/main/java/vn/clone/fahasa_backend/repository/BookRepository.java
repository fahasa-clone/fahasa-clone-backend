package vn.clone.fahasa_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookSpec;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    Optional<Book> findBySlug(String slug);

    @Query("SELECT b.id FROM Book b WHERE b.category.id IN :categoryIds")
    List<Integer> findBookIdsByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);

    @Query("SELECT bs FROM BookSpec bs WHERE bs.book.id IN :bookIds AND bs.spec.id IN :specIds")
    List<BookSpec> findBookSpecsByBookIdsAndSpecIds(@Param("bookIds") List<Integer> bookIds,
                                                    @Param("specIds") List<Integer> specIds);
}
