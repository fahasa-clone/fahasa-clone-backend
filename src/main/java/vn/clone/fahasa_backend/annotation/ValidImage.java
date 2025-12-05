package vn.clone.fahasa_backend.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.clone.fahasa_backend.annotation.validator.ImageValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ImageValidator.class)
@Documented
public @interface ValidImage {
    
    String message() default "File must be an image (jpg, jpeg, png, gif, webp, bmp)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    // Maximum file size (bytes)
    long maxSize() default 5242880; // 5MB default
    
    // Allowed formats
    String[] allowedFormats() default {"jpg", "jpeg", "png", "gif", "webp", "bmp"};
}
