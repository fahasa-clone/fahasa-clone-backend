package vn.clone.fahasa_backend.domain.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayPalCartItem {

    @NotBlank
    private String name;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer quantity;

    public BigDecimal getTotal() {
        return price.multiply(new BigDecimal(quantity));
    }
}
