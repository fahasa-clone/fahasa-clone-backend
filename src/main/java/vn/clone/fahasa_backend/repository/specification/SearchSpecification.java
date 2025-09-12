package vn.clone.fahasa_backend.repository.specification;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private static <E extends Enum<E>> E getEnumConstant(Class<E> enumClass, String value) {
        return Enum.valueOf(enumClass, value);
    }

    @SuppressWarnings("unchecked")
    private static <C extends Comparable<? super C>> C convertToComparableType(Class<C> type, String value) {
        Function<String, C> converter = (Function<String, C>) SearchSpecification.CONVERTERS.get(type);
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
        return converter.apply(value);
    }

    @SuppressWarnings("unchecked")
    private Object convert(Class<?> type, boolean isListReturnedType) {
        if (!isListReturnedType) {
            if (type.isEnum()) {
                return getEnumConstant((Class<? extends Enum>) type, this.value);
            }
            return convertToComparableType((Class<? extends Comparable>) type, this.value);
        }
        Pattern pattern = Pattern.compile("\\[(?<content>.+)]");
        Matcher matcher = pattern.matcher(this.value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Wrong format, format is [value1, value2,...]");
        }
        String[] values = matcher.group("content")
                                 .split(", ");
        List<Object> result = new ArrayList<>();
        for (String value : values) {
            if (type.isEnum()) {
                result.add(getEnumConstant((Class<? extends Enum>) type, value));
            } else {
                result.add(convertToComparableType((Class<? extends Comparable>) type, value));
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = root.get(this.name[0]);
        for (int i = 1; i < this.name.length; i++) {
            path = path.get(this.name[i]);
        }

        Class<?> type = path.getJavaType();
        Object parsedValue = switch (this.operator) {
            case "is" -> null;
            case "in", "not in" -> convert(type, true);
            default -> convert(type, false);
        };

        return switch (this.operator) {
            case ":" -> criteriaBuilder.equal(path, parsedValue);
            case "!" -> criteriaBuilder.notEqual(path, parsedValue);
            case ">" -> criteriaBuilder.greaterThan((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case "<" -> criteriaBuilder.lessThan((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case ">:" -> criteriaBuilder.greaterThanOrEqualTo((Path<? extends Comparable>) path,
                                                              (Comparable) parsedValue);
            case "<:" -> criteriaBuilder.lessThanOrEqualTo((Path<? extends Comparable>) path, (Comparable) parsedValue);
            case "~" -> criteriaBuilder.like((Expression<String>) path, "%" + parsedValue + "%");
            case "is" -> switch (this.value) {
                case "null" -> path.isNull();
                case "not null" -> path.isNotNull();
                case "empty" -> criteriaBuilder.isEmpty((Path<? extends Collection>) path);
                case "not empty" -> criteriaBuilder.isNotEmpty((Path<? extends Collection>) path);
                default -> throw new IllegalStateException("Unexpected value: " + this.operator + this.value);
            };
            case "in" -> path.in((Collection<?>) parsedValue);
            case "not in" -> criteriaBuilder.not(path.in((Collection<?>) parsedValue));
            default -> throw new IllegalStateException("Unexpected value: " + this.operator);
        };
    }
}
