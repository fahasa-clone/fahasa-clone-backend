package vn.clone.fahasa_backend.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotBlank(message = "email is required!")
    private String email;

    // @SizedNotBlank(message = "Password is required!", message1 = "Password length must be between 4 and 100!", min = 4, max = 100)
    @NotBlank
    @Size(min = 4, max = 100)
    private String password;
}
