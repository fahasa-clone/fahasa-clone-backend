package vn.clone.fahasa_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private int id;

    private String email;

    private String firstName;

    private String lastName;
}
