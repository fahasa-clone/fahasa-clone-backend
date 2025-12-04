package vn.clone.fahasa_backend.annotation.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import vn.clone.fahasa_backend.annotation.ValidImage;

@Slf4j
public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private long maxSize;
    private Set<String> allowedFormats;

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        maxSize = constraintAnnotation.maxSize();
        allowedFormats = new HashSet<>(Arrays.asList(constraintAnnotation.allowedFormats()));
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Allow null (use @NotNull if required)
        if (file == null || file.isEmpty()) {
            return true;
        }

        // Check file size
        if (file.getSize() > maxSize) {
            addConstraintViolation(context,
                                   String.format("Image size must not exceed %d MB", maxSize / (1024 * 1024)));
            return false;
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (!isValidImageMimeType(contentType)) {
            addConstraintViolation(context, "Invalid image format");
            return false;
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (!isValidImageExtension(filename)) {
            addConstraintViolation(context, "Invalid file extension");
            return false;
        }

        return true;
    }

    /**
     * Check if MIME type is a valid image type
     */
    private boolean isValidImageMimeType(String contentType) {
        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/");
    }

    /**
     * Check if file extension is valid
     */
    private boolean isValidImageExtension(String filename) {
        if (filename == null) {
            return false;
        }

        String fileExtension = filename.substring(filename.lastIndexOf(".") + 1)
                                       .toLowerCase();
        return allowedFormats.contains(fileExtension);
    }

    /**
     * Add constraint violation to context
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}
