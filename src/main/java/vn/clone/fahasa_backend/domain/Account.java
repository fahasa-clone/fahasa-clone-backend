package vn.clone.fahasa_backend.domain;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;

import vn.clone.fahasa_backend.configuration.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.Gender;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account extends AbstractEntity {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits long and contain only numbers.")
    private String phone;
    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    private Gender gender;
    private Date birthday;
    private boolean isActivated;
    private String activationKey;
    private String token;
    private boolean isOauth2;
}