package vn.clone.fahasa_backend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.core.annotation.AliasFor;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(SizedNotBlank.List.class)
@Documented
@Constraint(validatedBy = {})
// @NotBlank(message = "{jakarta.validation.constraints.NotBlank.message}")
// @Size(message = "{jakarta.validation.constraints.Size.message}")
// @NotBlank(message = "Username cannot be blank")
// @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
@NotBlank
@Size
public @interface SizedNotBlank {

    // 1. Default message (can be overridden)
    @AliasFor(value = "message", annotation = NotBlank.class)
    String message() default "Invalid username format";

    @AliasFor(value = "message", annotation = Size.class)
    String message1() default "Invalid username format";

    // 2. Required for validation groups
    Class<?>[] groups() default {};

    // 3. Required for carrying metadata
    Class<? extends Payload>[] payload() default {};

    /**
     * @return size the element must be higher or equal to
     */
    @AliasFor(annotation = Size.class, attribute = "min")
    int min() default 0;

    /**
     * @return size the element must be lower or equal to
     */
    @AliasFor(annotation = Size.class, attribute = "max")
    int max() default Integer.MAX_VALUE;

    /**
     * Defines several {@link SizedNotBlank} annotations on the same element.
     *
     * @see SizedNotBlank
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {

        SizedNotBlank[] value();
    }
}
