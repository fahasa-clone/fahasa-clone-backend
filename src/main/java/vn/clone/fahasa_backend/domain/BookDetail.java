package vn.clone.fahasa_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;

import vn.clone.fahasa_backend.config.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.BookLayout;

@Entity
@Table(name = "book_details")
@Getter
@Setter
public class BookDetail extends AbstractEntity {
    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "book_height")
    private Float bookHeight;

    @Column(name = "book_width")
    private Float bookWidth;

    @Column(name = "book_thickness")
    private Float bookThickness;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "layout")
    // @Enumerated(EnumType.STRING)
    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    // @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BookLayout layout;

    @Column(name = "description")
    private String description;
}
