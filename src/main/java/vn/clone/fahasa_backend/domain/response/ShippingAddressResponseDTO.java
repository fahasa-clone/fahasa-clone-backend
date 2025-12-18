package vn.clone.fahasa_backend.domain.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ShippingAddressResponseDTO {

    private Integer id;

    private Integer wardId;

    private String receiverName;

    private String receiverPhone;

    private String provinceName;

    private String districtName;

    private String wardName;

    private String detailAddress;

    private Boolean isDefault;

}
