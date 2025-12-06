package vn.clone.fahasa_backend.domain.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateBookImagesRequest {

    /**
     * List of images to delete (by id and imageUrl)
     */
    @Valid
    private List<ImageToDelete> imagesToDelete;

    /**
     * List of new images to upload (MultipartFile with imageOrder)
     */
    @Valid
    private List<ImageToUpload> imagesToUpload;

    /**
     * List of existing images to update order (by id and new imageOrder)
     */
    @Valid
    private List<ImageToUpdateOrder> imagesToUpdateOrder;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ImageToDelete {
        @NotNull(message = "Image ID is required")
        @Min(value = 1, message = "Image ID must be >= 1")
        private Integer id;

        @NotNull(message = "Image URL is required")
        private String imageUrl;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ImageToUpload {
        @NotNull(message = "Image file is required")
        private MultipartFile file;

        @NotNull(message = "Image order is required")
        @Min(value = 1, message = "Image order must be >= 1")
        private Integer imageOrder;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ImageToUpdateOrder {
        @NotNull(message = "Image ID is required")
        @Min(value = 1, message = "Image ID must be >= 1")
        private Integer id;

        @NotNull(message = "Image order is required")
        @Min(value = 1, message = "Image order must be >= 1")
        private Integer imageOrder;
    }
}
