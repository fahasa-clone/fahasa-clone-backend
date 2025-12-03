package vn.clone.fahasa_backend.domain.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookDTO {
    private Integer id;
    private String name;
    private long price;
    private Integer discountPercentage;
    private Integer discountAmount;
    private Float averageRating;
    private Integer ratingCount;
    private Integer stock;
    private boolean deleted;
    private String imagePath;
}
