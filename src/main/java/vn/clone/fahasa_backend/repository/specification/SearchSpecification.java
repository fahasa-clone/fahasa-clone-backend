package vn.clone.fahasa_backend.repository.specification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SearchSpecification<T> implements Specification<T> {
    private static final Map<Class<?>, Function<String, ? extends Comparable<?>>> CONVERTERS = Map.ofEntries(
            Map.entry(Integer.class, Integer::parseInt),
            Map.entry(Long.class, Long::parseLong),
            Map.entry(Double.class, Double::parseDouble),
            Map.entry(Float.class, Float::parseFloat),
            Map.entry(Short.class, Short::parseShort),
            Map.entry(Byte.class, Byte::parseByte),
            Map.entry(String.class, input -> {
                Pattern pattern = Pattern.compile("^(?<quote>[\"'])(?<content>.*)\\k<quote>$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("String text must be wrapped in single quote!");
                }
                return matcher.group("content");
            }),
            Map.entry(BigDecimal.class, BigDecimal::new),
            Map.entry(BigInteger.class, BigInteger::new),
            Map.entry(Instant.class, Instant::parse),
            Map.entry(ZonedDateTime.class, ZonedDateTime::parse),
            Map.entry(LocalDate.class, LocalDate::parse),
            Map.entry(LocalDateTime.class, LocalDateTime::parse)
    );

    private final String[] name;
    private final String operator;
    private final String value;

    public SearchSpecification(String name, String operator, String value) {
        this.name = name.split("\\.");
        this.operator = operator;
        this.value = value;
    }

    // @SuppressWarnings("unchecked")
    private static Object convert(Class<?> type, String value) {
        if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, value);
        }
        return convertToComparableType((Class<? extends Comparable>) type, value);
    }

    private static <C extends Comparable<? super C>> C convertToComparableType(Class<C> type, String value) {
        Function<String, C> converter = (Function<String, C>) SearchSpecification.CONVERTERS.get(type);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return converter.apply(value);
    }

    @Override
    // @SuppressWarnings("unchecked")
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = root.get(this.name[0]);
        for (int i = 1; i < this.name.length; i++) {
            path = path.get(this.name[i]);
        }
        Class<?> type = path.getJavaType();

        Object parsedValue = switch (this.operator) {
            case ">", "<", ">:", "<:", "~" -> convertToComparableType((Class<? extends Comparable>) type, this.value);
            default -> convert(type, this.value);
        };

        return switch (this.operator) {
            case ":" -> criteriaBuilder.equal(path, parsedValue);
            case "!" -> criteriaBuilder.notEqual(path, parsedValue);
            case ">" -> criteriaBuilder.greaterThan((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case "<" -> criteriaBuilder.lessThan((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case ">:" ->
                    criteriaBuilder.greaterThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case "<:" -> criteriaBuilder.lessThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case "~" -> criteriaBuilder.like((Expression<String>) path, "%" + parsedValue + "%");
            case "is" -> switch (this.value) {
                case "null" -> path.isNull();
                case "not null" -> path.isNotNull();
                case "empty" -> criteriaBuilder.isEmpty((Path<? extends Collection>) path);
                case "not empty" -> criteriaBuilder.isNotEmpty((Path<? extends Collection>) path);
                default -> throw new IllegalStateException("Unexpected value: " + this.operator + this.value);
            };
            default -> throw new IllegalStateException("Unexpected value: " + this.operator);
        };
    }
}
