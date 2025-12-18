package vn.clone.fahasa_backend.domain.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import vn.clone.fahasa_backend.util.constant.BookLayout;

@Builder
@Getter
public class FullBookDTO {

    private Integer id;
    private String name;
    private int categoryId;
    private long price;
    private Integer discountPercentage;
    private Integer discountAmount;
    private Float averageRating;
    private Integer ratingCount;
    private Integer stock;

    // Relationship mappings
    private BookDetailDTO bookDetail;
    private List<BookImageDTO> bookImages;
    private List<AuthorDTO> authors;
    private List<BookSpecDTO> bookSpecs;

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

        // Relationship mappings
        private PublisherDTO publisher;

        @Builder
        @Getter
        public static class PublisherDTO {
            private Integer id;
            private String name;
        }
    }

    @Builder
    @Getter
    public static class BookImageDTO {
        private Integer id;
        private String imagePath;
        private Integer imageOrder;
    }

    @Builder
    @Getter
    public static class AuthorDTO {
        private Integer id;
        private String name;
    }

    @Builder
    @Getter
    @Setter
    public static class BookSpecDTO {

        private Integer specId;

        private String specName;

        private String value;
    }
}
