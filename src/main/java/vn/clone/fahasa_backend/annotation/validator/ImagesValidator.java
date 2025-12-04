package vn.clone.fahasa_backend.annotation.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import vn.clone.fahasa_backend.annotation.ValidImages;

@Slf4j
public class ImagesValidator implements ConstraintValidator<ValidImages, List<MultipartFile>> {

    private long maxSize;
    private Set<String> allowedFormats;
    private int maxCount;

    @Override
    public void initialize(ValidImages constraintAnnotation) {
        maxSize = constraintAnnotation.maxSize();
        allowedFormats = new HashSet<>(Arrays.asList(constraintAnnotation.allowedFormats()));
        maxCount = constraintAnnotation.maxCount();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        // Allow null or empty list
        if (files == null || files.isEmpty()) {
            return true;
        }

        // Check file count
        if (files.size() > maxCount) {
            addConstraintViolation(context,
                                   String.format("Number of images must not exceed %d", maxCount));
            return false;
        }

        // Validate each file
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            if (file == null || file.isEmpty()) {
                continue;
            }

            // Check file size
            if (file.getSize() > maxSize) {
                addConstraintViolation(context,
                                       String.format("Image %d: Size must not exceed %d MB",
                                                     i + 1, maxSize / (1024 * 1024)));
                return false;
            }

            // Check MIME type
            if (!isValidImageMimeType(file.getContentType())) {
                addConstraintViolation(context,
                                       String.format("Image %d: Invalid format", i + 1));
                return false;
            }

            // Check file extension
            if (!isValidImageExtension(file.getOriginalFilename())) {
                addConstraintViolation(context,
                                       String.format("Image %d: Invalid file extension", i + 1));
                return false;
            }
        }

        return true;
    }

    private boolean isValidImageMimeType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    private boolean isValidImageExtension(String filename) {
        if (filename == null) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return allowedFormats.contains(extension);
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}
