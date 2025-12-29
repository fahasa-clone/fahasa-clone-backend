package vn.clone.fahasa_backend.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import vn.clone.fahasa_backend.config.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.OrderStatus;
import vn.clone.fahasa_backend.util.constant.PaymentMethod;
import vn.clone.fahasa_backend.util.constant.ShippingMethod;

@Entity
@Table(name = "orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "sub_total")
    private Long subTotal;

    @Column(name = "shipping_fee")
    private Long shippingFee;

    @Column(name = "grand_total")
    private Long grandTotal;

    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    @Column(name = "shipping_method")
    private ShippingMethod shippingMethod;

    @Column(name = "public_id", columnDefinition = "uuid", insertable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID publicId;

    @Column(name = "order_reference")
    private String orderReference;

    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    @Column(name = "current_status")
    private OrderStatus currentStatus;

    @OneToMany(fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               mappedBy = "order")
    private List<OrderState> orderStates;

    @OneToMany(fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               mappedBy = "order")
    private List<OrderDetail> orderDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}