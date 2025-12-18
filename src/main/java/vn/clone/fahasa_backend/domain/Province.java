package vn.clone.fahasa_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provinces")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Province {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

}