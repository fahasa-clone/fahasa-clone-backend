package vn.clone.fahasa_backend.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Book extends AbstractEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private long price;

    @Column(name = "discount_percentage")
    private int discountPercentage;

    @Column(name = "discount_amount")
    private int discountAmount;

    @Column(name = "average_rating")
    private float averageRating;

    @Column(name = "rating_count")
    private int ratingCount;

    @Column(name = "stock")
    private int stock;

    @Column(name = "delete_status")
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // =========== Relationship mappings ===========
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE},
              mappedBy = "book")
    // @MapsId
    private BookDetail bookDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(cascade = {CascadeType.PERSIST},
               mappedBy = "book")
    private List<BookImage> bookImages;
}
