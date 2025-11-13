package vn.clone.fahasa_backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RefreshToken {

    @Id
    @Column(name = "refresh_token")
    private String token;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
