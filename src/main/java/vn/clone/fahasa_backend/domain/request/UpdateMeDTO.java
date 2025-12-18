package vn.clone.fahasa_backend.domain.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import vn.clone.fahasa_backend.util.constant.Gender;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class UpdateMeDTO {

    private String email;

    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be 10 digits long and contain only numbers.")
    private String phone;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate birthday;
}
