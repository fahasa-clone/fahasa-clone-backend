package vn.clone.fahasa_backend.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;

import vn.clone.fahasa_backend.config.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

@Entity
@Table(name = "order_states")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

}
