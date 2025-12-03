package vn.clone.fahasa_backend.domain.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

import vn.clone.fahasa_backend.util.constant.BookLayout;

@Builder
@Getter
public class FullBookDTO {
    private Integer id;
    private String name;
    private long price;
    private Integer discountPercentage;
    private Integer discountAmount;
    private Float averageRating;
    private Integer ratingCount;
    private Integer stock;
    private boolean deleted;

    // Relationship mappings
    BookDetailDTO bookDetail;
    List<BookImageDTO> bookImages;

    // Inner classes
    @Builder
    @Getter
    public static class BookDetailDTO {
        private Integer publicationYear;
        private Integer weight;
        private Float bookHeight;
        private Float bookWidth;
        private Float bookThickness;
        private Integer pageCount;
        private BookLayout layout;
        private String description;
    }

    @Builder
    @Getter
    public static class BookImageDTO {
        private Integer id;
        private String imagePath;
        private Integer imageOrder;
    }
}
