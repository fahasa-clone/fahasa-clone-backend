package vn.clone.fahasa_backend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import vn.clone.fahasa_backend.annotation.SizedNotBlank;

@Getter
@Setter
public class ResetPasswordDTO {

    @NotBlank(message = "email is required!")
    private String email;

    @SizedNotBlank(message = "password is required!", min = 4, max = 100)
    private String newPassword;
}
