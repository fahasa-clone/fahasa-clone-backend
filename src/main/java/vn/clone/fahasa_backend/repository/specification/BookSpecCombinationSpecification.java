package vn.clone.fahasa_backend.repository.specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import vn.clone.fahasa_backend.domain.*;

/**
 * Specification to filter books by multiple BookSpec combinations.
 * <br>
 * Example:<br>
 * - Combination 1: specId = 1, value = "Test"<br>
 * - Combination 2: specId = 2, value = "Test 2"<br>
 * Result: Books that have BOTH combinations<br>
 * SQL: WHERE (spec_id = 1 AND value = 'Test') AND (spec_id = 2 AND value = 'Test 2')
 */
@RequiredArgsConstructor
public class BookSpecCombinationSpecification implements Specification<Book> {

    // List of combinations, each containing specId and value
    private final List<BookSpecCombination> combinations;

    public record BookSpecCombination(Integer specId, String value) {
    }

    @Override
    public Predicate toPredicate(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> combinationPredicates = new ArrayList<>();

        // Create a join
        Join<Book, BookSpec> bookSpecJoin = root.join(Book_.bookSpecs, JoinType.INNER);

        // For each combination, create a join and add conditions
        for (BookSpecCombination combo : combinations) {
            // Build conditions: specId = X AND value = Y
            Predicate specIdPredicate = cb.equal(
                    bookSpecJoin.get(BookSpec_.spec).get(Spec_.id),
                    combo.specId()
            );

            Predicate valuePredicate = cb.equal(
                    bookSpecJoin.get(BookSpec_.value),
                    combo.value()
            );

            // Combine these two conditions with AND
            Predicate combinedPredicate = cb.and(specIdPredicate, valuePredicate);
            combinationPredicates.add(combinedPredicate);
        }

        // If no combinations, return true
        if (combinationPredicates.isEmpty()) {
            return cb.conjunction();
        }

        // Combine all combinations with AND logic
        // This means: Book must have ALL combinations
        return cb.and(combinationPredicates.toArray(new Predicate[0]));
    }
}
