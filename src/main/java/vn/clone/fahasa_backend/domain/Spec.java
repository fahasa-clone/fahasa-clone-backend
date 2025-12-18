package vn.clone.fahasa_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "specifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Spec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
