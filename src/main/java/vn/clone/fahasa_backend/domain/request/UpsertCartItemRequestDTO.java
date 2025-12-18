package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpsertCartItemRequestDTO {
    @NotNull(message = "Book id is required")
    @Min(value = 1, message = "Book id must be greater than 0")
    private Integer bookId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Is clicked is required")
    private Boolean isClicked;
}
