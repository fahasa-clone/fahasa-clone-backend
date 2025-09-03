package vn.clone.fahasa_backend.repository.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;


public class SearchSpecification<T> implements Specification<T> {
    private String name;
    private String operator;
    private Object value;

    public SearchSpecification(String name, String operator, Object value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    public Predicate toPredicateTest(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query,
                                     @NonNull CriteriaBuilder builder) {
        // return switch (criteria.getOperation()) {
        //     case EQUALITY -> builder.equal(root.get(criteria.getKey()), Gender.valueOf(criteria.getValue().toString()));
        //     case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
        //     case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
        //     case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
        //     case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        //     case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
        //     case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
        //     case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        // };

        return null;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = root.get(this.name);

        return switch (this.operator) {
            case ":" -> criteriaBuilder.equal(root.get(this.name), this.value);
            case ">" -> {
                if (path.getJavaType().equals(Integer.class)) {
                    yield criteriaBuilder.greaterThan(root.get(this.name), (Integer) this.value);
                } else if (path.getJavaType().equals(Double.class)) {
                    yield criteriaBuilder.greaterThan(root.get(this.name), (Double) this.value);
                }
                yield criteriaBuilder.greaterThan(root.get(this.name), (Double) this.value);
            }
            case "<:" -> criteriaBuilder.lessThanOrEqualTo(root.get(this.name), (Integer) this.value);
            case ">:" -> criteriaBuilder.greaterThanOrEqualTo(root.get(this.name), (Integer) this.value);
            case "~" -> criteriaBuilder.like((Path<String>) path, "%" + this.value + "%");
            default -> throw new IllegalStateException("Unexpected value: " + this.operator);
        };
    }
}
