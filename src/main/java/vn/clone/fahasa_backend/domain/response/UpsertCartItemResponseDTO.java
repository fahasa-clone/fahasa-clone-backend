package vn.clone.fahasa_backend.domain.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpsertCartItemResponseDTO {
    private Integer bookId;
    private String bookName;
    private Integer quantity;
    private Boolean isClicked;
}
