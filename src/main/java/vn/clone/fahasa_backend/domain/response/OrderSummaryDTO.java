package vn.clone.fahasa_backend.domain.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

import vn.clone.fahasa_backend.util.constant.OrderStatus;

@Builder

@Getter
public class OrderSummaryDTO {
    private UUID publicId;

    private String orderReference;

    private Instant createdAt;

    private Instant updatedAt;

    private OrderStatus currentStatus;

    private Integer totalQuantity;

    private Long grandTotal;
}
