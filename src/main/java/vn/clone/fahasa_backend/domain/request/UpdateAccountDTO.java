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
public class UpdateAccountDTO extends UpdateMeDTO {

    private String password;

    @NotBlank
    private Integer roleId;

    @NotBlank
    private Boolean isActivated;
}
