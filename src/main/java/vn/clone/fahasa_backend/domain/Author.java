package vn.clone.fahasa_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "authors")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
