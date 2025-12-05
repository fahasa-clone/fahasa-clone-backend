package vn.clone.fahasa_backend.repository;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookImage;
import vn.clone.fahasa_backend.domain.response.BookDTO;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    public Page<BookDTO> findAllBooksWithFirstImage(Specification<Book> specification, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query for data
        CriteriaQuery<BookDTO> query = cb.createQuery(BookDTO.class);
        Root<Book> book = query.from(Book.class);

        // Left join with BookImage and filter for imageOrder = 1
        Join<Book, BookImage> imageJoin = book.join("bookImages", JoinType.LEFT);
        imageJoin.on(cb.equal(imageJoin.get("imageOrder"), 1));

        // Select and construct BookDTO
        query.select(cb.construct(BookDTO.class,
                                  book.get("id"),
                                  book.get("name"),
                                  book.get("price"),
                                  book.get("discountPercentage"),
                                  book.get("discountAmount"),
                                  book.get("averageRating"),
                                  book.get("ratingCount"),
                                  book.get("stock"),
                                  book.get("deleted"),
                                  imageJoin.get("imagePath")
        ));

        // Apply specification predicate
        if (specification != null) {
            Predicate predicate = specification.toPredicate(book, query, cb);
            predicate = cb.and(predicate, cb.equal(book.get("deleted"), false));
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // Apply sorting
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : pageable.getSort()) {
                if (sortOrder.isAscending()) {
                    orders.add(cb.asc(book.get(sortOrder.getProperty())));
                } else {
                    orders.add(cb.desc(book.get(sortOrder.getProperty())));
                }
            }
            query.orderBy(orders);
        }

        // Execute query with pagination
        TypedQuery<BookDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<BookDTO> results = typedQuery.getResultList();

        // Count query for total elements
        long total = countQuery(specification);

        return new PageImpl<>(results, pageable, total);
    }

    private long countQuery(Specification<Book> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Book> book = countQuery.from(Book.class);

        countQuery.select(cb.count(book));

        if (specification != null) {
            Predicate predicate = specification.toPredicate(book, countQuery, cb);
            if (predicate != null) {
                countQuery.where(predicate);
            }
        }

        return entityManager.createQuery(countQuery)
                            .getSingleResult();
    }
}