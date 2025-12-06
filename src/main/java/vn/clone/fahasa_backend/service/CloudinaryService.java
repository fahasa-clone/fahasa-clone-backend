package vn.clone.fahasa_backend.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final Cloudinary cloudinary;

    /**
     * Upload an image file to Cloudinary with a custom name
     */
    public String uploadImage(MultipartFile file, String folder, String publicId) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "quality", "auto",
                    "overwrite", true
            );
            if (publicId != null) {
                options.put("public_id", publicId);
            }

            Map<?, ?> uploadResult = cloudinary.uploader()
                                               .upload(file.getBytes(), options);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    /**
     * Upload multiple image files to Cloudinary using names derived from the book slug
     */
    public List<String> uploadImages(List<MultipartFile> files, String folder, String bookSlug) {
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (!file.isEmpty()) {
                // Generate file name: {bookSlug}-{imageIndex}-{randomNumber}
                String publicId = generatePublicId(bookSlug, i);
                String url = uploadImage(file, folder, publicId);
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    /**
     * Delete image on Cloudinary by public_id
     */
    public void deleteImage(String publicId) {
        try {
            if (publicId == null || publicId.trim().isEmpty()) {
                log.warn("Public ID is null or empty");
                return;
            }

            Map<String, Object> options = ObjectUtils.asMap(
                    "invalidate", true  // Invalidate CDN cache
            );

            Map<?, ?> result = cloudinary.uploader()
                                         .destroy(publicId, options);

            // Check response from Cloudinary
            Object resultObj = result.get("result");
            if ("ok".equals(resultObj)) {
                log.info("Image successfully deleted: {}", publicId);
            } else if ("not found".equals(resultObj)) {
                log.warn("Image not found on Cloudinary: {}", publicId);
            } else {
                log.warn("Unexpected result when deleting image: {}", result);
            }
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary - publicId: {} - Error: {}",
                      publicId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete image on Cloudinary", e);
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     * <p>
     * URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{folder}/{public_id}.{format}
     */
    public String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            log.warn("URL is null or empty");
            return null;
        }

        String regex = "/v\\d+/(?<publicId>.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String publicId = matcher.group("publicId");
            if (publicId.contains(".")) {
                return publicId.substring(0, publicId.lastIndexOf('.'));
            }
            return publicId;
        }

        log.warn("Could not extract publicId from URL: {}", url);
        return null;
    }

    /**
     * Generate public_id from book slug and a random number
     * <p>
     * Format: {bookSlug}-{imageIndex}-{randomNumber}
     * <p>
     * Example: java-programming-0-7381256
     */
    public String generatePublicId(String bookSlug, int imageIndex) {
        long randomNumber = Math.abs(SECURE_RANDOM.nextLong());
        return String.format("%s-%d-%d", bookSlug, imageIndex, randomNumber);
    }
}
