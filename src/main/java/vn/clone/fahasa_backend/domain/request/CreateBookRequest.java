package vn.clone.fahasa_backend.domain.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import vn.clone.fahasa_backend.annotation.ValidImage;
import vn.clone.fahasa_backend.annotation.ValidImages;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateBookRequest {

    @NotBlank(message = "Book name cannot be empty")
    private String name;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater than 0")
    private Long price;

    @Min(value = 0, message = "Discount percentage must be at least 0")
    private Integer discountPercentage;

    @Min(value = 0, message = "Discount amount must be at least 0")
    private Integer discountAmount;

    @NotNull(message = "Stock quantity cannot be null")
    private Integer stock;

    @Positive
    private int categoryId;

    // BookDetail fields
    @Min(0)
    private Integer publicationYear;

    @Min(0)
    private Integer weight;

    @Min(0)
    private Float bookHeight;

    @Min(0)
    private Float bookWidth;

    @Min(0)
    private Float bookThickness;

    @Min(1)
    private Integer pageCount;

    private String layout;

    private String description;

    @NotNull(message = "Cover image is required")
    @ValidImage(message = "Cover image must be a valid image file (jpg, jpeg, png, gif, webp, bmp)")
    private MultipartFile coverImage;

    @ValidImages(maxCount = 2)
    private List<MultipartFile> images;
}
