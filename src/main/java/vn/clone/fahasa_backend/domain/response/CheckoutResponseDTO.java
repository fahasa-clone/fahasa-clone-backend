package vn.clone.fahasa_backend.domain.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CheckoutResponseDTO {
    List<CartItemResponseDTO> cartItems;
    Long subTotal;
    Long shippingFee;
    Long grandTotal;
}
