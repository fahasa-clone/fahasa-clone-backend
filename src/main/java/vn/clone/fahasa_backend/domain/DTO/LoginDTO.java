package vn.clone.fahasa_backend.domain.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    @NotBlank(message = "email is required!")
    private String email;

    @NotBlank(message = "password is required!")
    private String password;
}
