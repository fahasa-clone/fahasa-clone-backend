package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import vn.clone.fahasa_backend.annotation.SizedNotBlank;

@Getter
@Setter
public class VerifyOtpDTO {

    @NotBlank(message = "email is required!")
    private String email;

    @SizedNotBlank(message = "OTP is required!", min = 6, max = 6)
    private String otpValue;
}
