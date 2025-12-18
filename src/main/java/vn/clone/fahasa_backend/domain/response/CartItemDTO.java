package vn.clone.fahasa_backend.domain.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartItemDTO {
    private Integer cartItemId;
    private Integer bookId;
    private String bookName;
    private Long bookPrice;
    private Integer bookDiscountPercentage;
    private Integer bookDiscountAmount;
    private Integer bookStock;
    private String bookImage;
    private Integer quantity;
    private Boolean isClicked;
}
