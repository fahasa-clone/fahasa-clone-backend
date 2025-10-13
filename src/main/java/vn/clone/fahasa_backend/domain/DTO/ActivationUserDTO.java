package vn.clone.fahasa_backend.domain.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ActivationUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String activationKey;

    public ActivationUserDTO() {
    }

    public ActivationUserDTO(String email, String firstName, String lastName, String activationKey) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.activationKey = activationKey;
    }
}