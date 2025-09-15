package vn.clone.fahasa_backend.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "books")
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
    private boolean deleteStatus;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // Relationship mappings
    @OneToOne(fetch = FetchType.LAZY)
    // @MapsId
    @JoinColumn(name = "id")
    private BookDetail bookDetail;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
