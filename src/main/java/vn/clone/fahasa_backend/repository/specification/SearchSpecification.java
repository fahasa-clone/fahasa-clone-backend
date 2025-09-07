package vn.clone.fahasa_backend.repository.specification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Function;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import vn.clone.fahasa_backend.util.constant.Gender;

@AllArgsConstructor
public class SearchSpecification<T> implements Specification<T> {
    private static final Map<Class<?>, Function<String, ? extends Comparable<?>>> CONVERTERS = Map.ofEntries(
            Map.entry(Integer.class, Integer::parseInt),
            Map.entry(Long.class, Long::parseLong),
            Map.entry(Double.class, Double::parseDouble),
            Map.entry(Float.class, Float::parseFloat),
            Map.entry(Short.class, Short::parseShort),
            Map.entry(Byte.class, Byte::parseByte),
            Map.entry(String.class, s -> s),
            Map.entry(BigDecimal.class, BigDecimal::new),
            Map.entry(BigInteger.class, BigInteger::new),
            Map.entry(Instant.class, Instant::parse),
            Map.entry(ZonedDateTime.class, ZonedDateTime::parse),
            Map.entry(LocalDate.class, LocalDate::parse),
            Map.entry(LocalDateTime.class, LocalDateTime::parse)
    );

    private String name;
    private String operator;
    private String value;

    public static <E extends Enum<E>> E getEnumConstant(Class<E> enumClass, String name) {
        return Enum.valueOf(enumClass, name);
    }

    // @SuppressWarnings("unchecked")
    private static <C extends Comparable<? super C>> C convert(Class<?> type, String value) {
        if (type.isEnum()) {
            Class<Gender> clazz = Gender.class;
            // return (C) Enum.valueOf((Class<? extends Enum>) type, value);
            Enum.valueOf(clazz, value);
            getEnumConstant(clazz, value);
        }
        Function<String, ?> converter = SearchSpecification.CONVERTERS.get(type);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return (C) converter.apply(value);
    }

    @Override
    // @SuppressWarnings("unchecked")
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = root.get(this.name);
        Class<?> type = path.getJavaType();

        return switch (this.operator) {
            case ":" -> {
                Comparable parsedValue = convert(type, this.value);
                yield criteriaBuilder.equal(root.get(this.name), parsedValue);
            }
            case "!" -> {
                Comparable parsedValue = convert(type, this.value);
                yield criteriaBuilder.notEqual(root.get(this.name), parsedValue);
            }
            case ">" -> {
                Comparable parsedValue = convert(type, this.value);
                yield criteriaBuilder.greaterThan(root.get(this.name), parsedValue);
            }
            case "<" -> {
                Comparable parsedValue = convert(type, this.value);
                yield criteriaBuilder.lessThan(root.get(this.name), parsedValue);
            }
            case ">:" -> {
                Comparable parsedValue = convert(type, this.value);
                yield criteriaBuilder.greaterThanOrEqualTo(root.get(this.name), parsedValue);
            }
            case "<:" -> {
                Comparable parsedValue = convert(type, this.value);
                yield criteriaBuilder.lessThanOrEqualTo(root.get(this.name), parsedValue);
            }
            case "~" -> criteriaBuilder.like(root.get(this.name), "%" + this.value + "%");
            case "is" -> switch (this.value) {
                case "null" -> root.get(this.name).isNull();
                case "not null" -> root.get(this.name).isNotNull();
                case "empty" -> criteriaBuilder.isEmpty(root.get(this.name));
                case "not empty" -> criteriaBuilder.isNotEmpty(root.get(this.name));
                default -> throw new IllegalStateException("Unexpected value: " + this.operator + this.value);
            };
            default -> throw new IllegalStateException("Unexpected value: " + this.operator);
        };
    }
}
