package vn.clone.fahasa_backend.domain.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import vn.clone.fahasa_backend.util.constant.Gender;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class RegisterDTO {

    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    // @NotBlank(message = "first name is required!")
    private String firstName;

    // @NotBlank(message = "last name is required!")
    private String lastName;

    // @NotBlank(message = "phone is required!")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits long and contain only numbers.")
    private String phone;

    // @NotBlank(message = "gender is required!")
    private Gender gender;

    // @NotBlank(message = "birthday is required!")
    private LocalDate birthday;
}