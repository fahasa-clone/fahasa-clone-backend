package vn.clone.fahasa_backend.domain.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InitResetPasswordDTO {

    private final String email;
}
