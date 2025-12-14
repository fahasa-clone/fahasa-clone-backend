package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {

    @NotBlank
    @Size(min = 6, max = 6)
    private String otpValue;

    @NotBlank
    @Size(min = 4, max = 100)
    private String newPassword;
}
