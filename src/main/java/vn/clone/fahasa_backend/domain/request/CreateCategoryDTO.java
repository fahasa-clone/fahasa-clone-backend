package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class CreateCategoryDTO {

    @NotBlank
    private String name;

    private String description;

    private String categoryIcon;

    private Integer parentId;
}
