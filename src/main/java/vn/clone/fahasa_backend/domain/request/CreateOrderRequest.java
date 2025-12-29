package vn.clone.fahasa_backend.domain.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private BigDecimal shippingFee;

    @NotNull
    private BigDecimal totalPrice;

    @Valid
    private List<PayPalCartItem> cart;
}
