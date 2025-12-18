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
import vn.clone.fahasa_backend.domain.response.CartItemDTO;
import vn.clone.fahasa_backend.repository.specification.SpecificationsBuilder;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<CartItemDTO> findAllCartItemByAccountId(Integer accountId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query for data
        CriteriaQuery<CartItemDTO> query = cb.createQuery(CartItemDTO.class);
        Root<CartItem> cartItem = query.from(CartItem.class);

        // Left join with book and bookImage and filter for imageOrder = 1
        Join<CartItem, Book> book = cartItem.join("book", JoinType.LEFT);
        Join<Book, BookImage> bookImage = book.join("bookImages", JoinType.LEFT);
        bookImage.on(cb.equal(bookImage.get("imageOrder"), 1));

        query.select(cb.construct(CartItemDTO.class,
                                  cartItem.get("id"),
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
        Specification<CartItem> specification = SpecificationsBuilder.hasAccountId(accountId);
        Predicate predicate = specification.toPredicate(cartItem, query, cb);
        query.where(predicate);

        // Apply sorting
        Order order = cb.desc(cartItem.get("id"));
        query.orderBy(order);

        // Execute query
        return entityManager.createQuery(query).getResultList();
    }
}
