package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySpecDTO {

    @NotNull
    private Integer specId;

    private Boolean isFiltered;
}
