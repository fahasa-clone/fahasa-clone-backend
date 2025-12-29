package vn.clone.fahasa_backend.util.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentMethod {

    CASH("cash"),
    PAYPAL("paypal"),
    VNPAY("vnpay");

    private final String value;
}
