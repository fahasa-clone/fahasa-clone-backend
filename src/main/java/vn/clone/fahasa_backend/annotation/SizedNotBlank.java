package vn.clone.fahasa_backend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
@NotBlank
@Size
public @interface SizedNotBlank {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return size the element must be higher or equal to
     */
    @AliasFor(annotation = Size.class)
    int min() default 0;

    /**
     * @return size the element must be lower or equal to
     */
    @AliasFor(annotation = Size.class)
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
