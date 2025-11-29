package vn.clone.fahasa_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "refresh_token", unique = true, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
