package vn.clone.fahasa_backend.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "books")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

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
    @OneToOne(mappedBy = "book",
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    // @MapsId
    private BookDetail bookDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book",
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               orphanRemoval = true)
    private List<BookImage> bookImages;
}
