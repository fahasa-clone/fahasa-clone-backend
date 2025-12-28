package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import vn.clone.fahasa_backend.util.constant.PaymentMethod;
import vn.clone.fahasa_backend.util.constant.ShippingMethod;

@Getter
public class OrderRequestDTO {
    @NotNull(message = "Shipping address id is required!")
    @Min(value = 1, message = "Shipping address id must be at least 1")
    private Integer shippingAddressId;

    @NotNull(message = "Payment method is required!")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Shipping method is required!")
    private ShippingMethod shippingMethod;

    @NotNull(message = "Grand total are required!")
    private Long grandTotal;
}
