package vn.clone.fahasa_backend.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import vn.clone.fahasa_backend.util.constant.OrderStatus;
import vn.clone.fahasa_backend.util.constant.PaymentMethod;
import vn.clone.fahasa_backend.util.constant.ShippingMethod;

@Builder
@Getter
public class CreateOrderResponseDTO {
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Long subTotal;
    private Long shippingFee;
    private Long grandTotal;
    private PaymentMethod paymentMethod;
    private ShippingMethod shippingMethod;
    private String publicId;
    private String orderReference;
    private String currentStatus;
    private List<OrderDetailResponseDTO> orderDetails;
    private List<OrderStateResponseDTO> orderStates;

    @Builder
    @Getter
    public static class OrderDetailResponseDTO {
        private Integer bookId;
        private Integer quantity;
        private Long price;
    }

    @Builder
    @Getter
    public static class OrderStateResponseDTO {
        private Instant createdAt;
        private OrderStatus status;
    }
}
