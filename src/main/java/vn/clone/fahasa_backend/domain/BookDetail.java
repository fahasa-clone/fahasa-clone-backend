package vn.clone.fahasa_backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import vn.clone.fahasa_backend.configuration.LowercasePostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.BookLayout;

@Entity
@Table(name = "book_details")
@Getter
@Setter
public class BookDetail extends AbstractEntity {
    @Column(name = "publication_year")
    private int publicationYear;

    @Column(name = "weight")
    private int weight;

    @Column(name = "book_height")
    private float bookHeight;

    @Column(name = "book_width")
    private float bookWidth;

    @Column(name = "book_thickness")
    private float bookThickness;

    @Column(name = "page_count")
    private int pageCount;

    @Column(name = "layout")
    @Enumerated(EnumType.STRING)
    @JdbcType(LowercasePostgreSQLEnumJdbcType.class)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BookLayout layout;

    @Column(name = "description")
    private String description;

    // Relationship mappings
    // @OneToOne(mappedBy = "bookDetail", fetch = FetchType.LAZY)
    // private Book book;
}
