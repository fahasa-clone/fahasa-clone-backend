package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ShippingAddressRequestDTO {
    @NotNull(message = "Ward id is required")
    @Min(value = 1, message = "Ward id must be greater than 0")
    private Integer wardId;

    @NotBlank(message = "Detail address is required")
    private String detailAddress;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be valid")
    private String receiverPhone;

    @NotNull(message = "Is default is required")
    private Boolean isDefault;
}
