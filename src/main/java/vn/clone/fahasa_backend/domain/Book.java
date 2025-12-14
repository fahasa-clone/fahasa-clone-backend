package vn.clone.fahasa_backend.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import vn.clone.fahasa_backend.config.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.BookLayout;

@Entity
@Table(name = "books")
@SecondaryTable(name = "book_details",
                pkJoinColumns = @PrimaryKeyJoinColumn(name = "book_id"))
@SQLDelete(sql = "UPDATE books SET delete_status = true WHERE id=?")
@SQLRestriction("delete_status = false")
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

    @Column(name = "slug")
    private String slug;

    @Column(name = "delete_status")
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // =========== Detail columns (from BookDetail) ===========
    @Column(name = "created_at", table = "book_details")
    private Instant createdAt;

    @Column(name = "updated_at", table = "book_details")
    private Instant updatedAt;

    @Column(name = "publication_year", table = "book_details")
    private Integer publicationYear;

    @Column(name = "weight", table = "book_details")
    private Integer weight;

    @Column(name = "book_height", table = "book_details")
    private Float bookHeight;

    @Column(name = "book_width", table = "book_details")
    private Float bookWidth;

    @Column(name = "book_thickness", table = "book_details")
    private Float bookThickness;

    @Column(name = "page_count", table = "book_details")
    private Integer pageCount;

    @Column(name = "layout", table = "book_details")
    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    private BookLayout layout;

    @Column(name = "description", table = "book_details")
    private String description;

    // =========== Relationship mappings ===========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", table = "book_details")
    private Publisher publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book",
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               orphanRemoval = true)
    private List<BookImage> bookImages;

    @ManyToMany
    @JoinTable(name = "books_authors",
               joinColumns = @JoinColumn(name = "book_id"),
               inverseJoinColumns = @JoinColumn(name = "author_id"))
    private List<Author> authors;
}
