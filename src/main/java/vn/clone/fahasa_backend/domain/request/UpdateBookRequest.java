package vn.clone.fahasa_backend.domain.request;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import vn.clone.fahasa_backend.annotation.MinForList;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateBookRequest {

    @NotBlank(message = "Book name cannot be empty")
    private String name;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be greater than 0")
    private Long price;

    @Min(value = 0, message = "Discount percentage must be at least 0")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private Integer discountPercentage;

    @Min(value = 0, message = "Discount amount must be at least 0")
    private Integer discountAmount;

    @NotNull(message = "Stock quantity cannot be null")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be a positive number")
    private Integer categoryId;

    // BookDetail fields
    @Min(value = 0, message = "Publication year cannot be negative")
    private Integer publicationYear;

    @Min(value = 0, message = "Weight cannot be negative")
    private Integer weight;

    @Min(value = 0, message = "Height cannot be negative")
    private Float bookHeight;

    @Min(value = 0, message = "Width cannot be negative")
    private Float bookWidth;

    @Min(value = 0, message = "Thickness cannot be negative")
    private Float bookThickness;

    @Min(value = 1, message = "Page count must be at least 1")
    private Integer pageCount;

    private String layout;

    private String description;

    @NotNull(message = "Publisher ID is required")
    @Min(value = 1, message = "Publisher ID must be at least 1")
    private Integer publisherId;

    @NotNull(message = "Author IDs are required")
    @MinForList(value = 1, message = "Each Author ID must be at least 1")
    List<Integer> authorIds;
}
