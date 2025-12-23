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
import vn.clone.fahasa_backend.domain.response.BookDTO;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    Optional<Book> findBySlug(String slug);

    @Query("SELECT b.id FROM Book b WHERE b.category.id IN :categoryIds")
    List<Integer> findBookIdsByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);

    @Query("SELECT bs FROM BookSpec bs WHERE bs.book.id IN :bookIds")
    List<BookSpec> findBookSpecsByBookIds(@Param("bookIds") List<Integer> bookIds);

    @Query(name = "Book.searchByNameFullText", nativeQuery = true)
    List<BookDTO> searchByNameFullText(@Param("searchQuery") String searchQuery);

    @Query(value = "SELECT b.* FROM books b " +
                   "left join book_images bi on b.id = bi.book_id " +
                   "WHERE search_tsvector @@ plainto_tsquery('english', :searchQuery) " +
                   "ORDER BY ts_rank(search_tsvector, plainto_tsquery('english', :searchQuery)) DESC",
           nativeQuery = true)
    List<BookDTO> searchByNameFullText_(@Param("searchQuery") String searchQuery);

    // Alternative with ranking and limit
    @Query(value = "SELECT * FROM books WHERE search_tsvector @@ " +
                   "plainto_tsquery('english', :searchQuery) AND delete_status = false " +
                   "ORDER BY ts_rank(search_tsvector, plainto_tsquery('english', :searchQuery)) DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<Book> searchByNameFullTextWithLimit(@Param("searchQuery") String searchQuery,
                                             @Param("limit") int limit);
}
