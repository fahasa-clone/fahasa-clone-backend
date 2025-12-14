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
import vn.clone.fahasa_backend.domain.BookImage_;
import vn.clone.fahasa_backend.domain.Book_;
import vn.clone.fahasa_backend.domain.response.BookDTO;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    public Page<BookDTO> findAllBooksWithFirstImage(Specification<Book> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BookDTO> query = cb.createQuery(BookDTO.class);
        Root<Book> book = query.from(Book.class);

        // Set up a base query with image join
        setupBaseQuery(query, book, cb);

        // Apply specification predicate
        applySpecification(specification, query, book, cb);

        // Apply sorting from Pageable
        applySorting(query, book, cb, pageable);

        // Execute and return paginated results
        return executePaginatedQuery(query, specification, pageable);
    }

    public Page<BookDTO> findNewestBooksWithFirstImage(Specification<Book> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BookDTO> query = cb.createQuery(BookDTO.class);
        Root<Book> book = query.from(Book.class);

        // Set up a base query with image join
        setupBaseQuery(query, book, cb);

        // Apply specification predicate
        applySpecification(specification, query, book, cb);

        // Apply sorting by created date
        Order createdDateOrder = cb.desc(book.get("createdAt"));
        query.orderBy(createdDateOrder);

        // Execute and return paginated results
        return executePaginatedQuery(query, specification, pageable);
    }

    // Helper methods

    private void setupBaseQuery(CriteriaQuery<BookDTO> query, Root<Book> book, CriteriaBuilder cb) {
        // Left join with BookImage and filter for imageOrder = 1
        Join<Book, BookImage> imageJoin = book.join(Book_.bookImages, JoinType.LEFT);
        imageJoin.on(cb.equal(imageJoin.get(BookImage_.imageOrder), 1));

        // Select and construct BookDTO
        query.select(cb.construct(BookDTO.class,
                                  book.get(Book_.id),
                                  book.get(Book_.name),
                                  book.get(Book_.price),
                                  book.get(Book_.discountPercentage),
                                  book.get(Book_.discountAmount),
                                  book.get(Book_.averageRating),
                                  book.get(Book_.ratingCount),
                                  book.get(Book_.stock),
                                  imageJoin.get(BookImage_.imagePath)
        ));
    }

    private void applySpecification(Specification<Book> specification, CriteriaQuery<?> query,
                                    Root<Book> book, CriteriaBuilder cb) {
        if (specification != null) {
            Predicate predicate = specification.toPredicate(book, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
    }

    private void applySorting(CriteriaQuery<?> query, Root<Book> book, CriteriaBuilder cb, Pageable pageable) {
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
    }

    private Page<BookDTO> executePaginatedQuery(CriteriaQuery<BookDTO> query,
                                                Specification<Book> specification,
                                                Pageable pageable) {
        TypedQuery<BookDTO> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<BookDTO> results = typedQuery.getResultList();
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