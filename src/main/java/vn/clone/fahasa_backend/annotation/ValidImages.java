package vn.clone.fahasa_backend.annotation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import vn.clone.fahasa_backend.annotation.validator.ImagesValidator;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImagesValidator.class)
@Documented
public @interface ValidImages {

    String message() default "Files must be valid images (jpg, jpeg, png, gif, webp, bmp)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxSize() default 5242880; // 5MB

    String[] allowedFormats() default {"jpg", "jpeg", "png", "gif", "webp", "bmp"};

    int maxCount() default 10; // Maximum 10 images
}
