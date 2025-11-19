package vn.clone.fahasa_backend.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "otps")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "account_id", nullable = false, unique = true)
    private Integer accountId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash; // Store a hash, not the plain text

    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    @Column(name = "attempts", nullable = false)
    private int attempts;
}
