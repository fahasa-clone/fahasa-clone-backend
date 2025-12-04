package vn.clone.fahasa_backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_images")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "image_order")
    private int imageOrder;

    // =========== Relationship mappings ===========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private Book book;
}
