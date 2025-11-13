package vn.clone.fahasa_backend.domain.DTO;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;

import vn.clone.fahasa_backend.annotation.SizedNotBlank;
import vn.clone.fahasa_backend.config.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.Gender;

@Getter
@Setter
public class RegisterDTO {
    @SizedNotBlank(message = "email is required!")
    private String email;

    @SizedNotBlank(message = "password is required!", min = 4, max = 100)
    private String password;

    // @NotBlank(message = "first name is required!")
    @JsonProperty("first_name")
    private String firstName;

    // @NotBlank(message = "last name is required!")
    @JsonProperty("last_name")
    private String lastName;

    // @NotBlank(message = "phone is required!")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits long and contain only numbers.")
    private String phone;

    // @NotBlank(message = "gender is required!")
    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    private Gender gender;

    // @NotBlank(message = "birthday is required!")
    private Date birthday;
}