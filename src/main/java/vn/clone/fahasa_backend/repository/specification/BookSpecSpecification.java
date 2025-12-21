package vn.clone.fahasa_backend.repository.specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.BookSpec;

@RequiredArgsConstructor
public class BookSpecSpecification implements Specification<Book> {

    private final List<Integer> specIds;

    private final List<String> values;

    @Override
    public Predicate toPredicate(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        // Join Book with BookSpec
        Join<Book, BookSpec> bookSpecJoin = root.join("bookSpecs", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        // Add filter: specId in (1, 2)
        if (specIds != null && !specIds.isEmpty()) {
            Path<Integer> specIdPath = bookSpecJoin.get("spec").get("id");
            predicates.add(specIdPath.in(specIds));
        }

        // Add filter: value in ("Hello", "Hi")
        if (values != null && !values.isEmpty()) {
            Path<String> valuePath = bookSpecJoin.get("value");
            predicates.add(valuePath.in(values));
        }

        // Combine with AND logic
        if (predicates.isEmpty()) {
            return criteriaBuilder.conjunction();
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
