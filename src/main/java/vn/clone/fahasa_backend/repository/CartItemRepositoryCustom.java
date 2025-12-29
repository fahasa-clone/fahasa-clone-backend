package vn.clone.fahasa_backend.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookImage;
import vn.clone.fahasa_backend.domain.CartItem;
import vn.clone.fahasa_backend.domain.response.CartItemResponseDTO;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<CartItemResponseDTO> findAllCartItems(Specification<CartItem> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query for data
        CriteriaQuery<CartItemResponseDTO> query = cb.createQuery(CartItemResponseDTO.class);
        Root<CartItem> cartItem = query.from(CartItem.class);

        // Left join with book and bookImage and filter for imageOrder = 1
        Join<CartItem, Book> book = cartItem.join("book", JoinType.LEFT);
        Join<Book, BookImage> bookImage = book.join("bookImages", JoinType.LEFT);
        bookImage.on(cb.equal(bookImage.get("imageOrder"), 1));

        query.select(cb.construct(CartItemResponseDTO.class,
                                  book.get("id"),
                                  book.get("name"),
                                  book.get("price"),
                                  book.get("discountPercentage"),
                                  book.get("discountAmount"),
                                  book.get("stock"),
                                  bookImage.get("imagePath"),
                                  cartItem.get("quantity"),
                                  cartItem.get("isClicked")
        ));

        // Apply specification predicate get cart by account id
        if (spec != null) {
            Predicate predicate = spec.toPredicate(cartItem, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }

        // Apply sorting
        Order order = cb.desc(cartItem.get("id"));
        query.orderBy(order);

        // Execute query
        return entityManager.createQuery(query).getResultList();
    }

    public List<CartItem> findAllCartItemClicked(Specification<CartItem> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<CartItem> query = cb.createQuery(CartItem.class);
        Root<CartItem> cartItem = query.from(CartItem.class);
        cartItem.join("book", JoinType.LEFT);

        Specification<CartItem> clicked = SpecificationsBuilder.isClicked();
        Specification<CartItem> specification = clicked.and(spec);
        Predicate predicate = specification.toPredicate(cartItem, query, cb);
        query.where(predicate);

        query.select(cb.construct(CartItem.class,
                                  cartItem.get("id"),
                                  cartItem.get("quantity"),
                                  cartItem.get("isClicked"),
                                  cartItem.get("book")
        ));

        return entityManager.createQuery(query).getResultList();
    }
}
