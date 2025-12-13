package vn.clone.fahasa_backend.domain;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;

import vn.clone.fahasa_backend.config.CustomPostgreSQLEnumJdbcType;
import vn.clone.fahasa_backend.util.constant.Gender;

@Entity
@Table(name = "accounts")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Account extends AbstractEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits long and contain only numbers.")
    @Column(name = "phone")
    private String phone;

    @JdbcType(CustomPostgreSQLEnumJdbcType.class)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "is_activated", nullable = false)
    private boolean isActivated;

    @Column(name = "activation_key")
    private String activationKey;

    // =========== Relationship mappings ===========
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // =========== Transient fields ===========
    @Transient
    private String otpValue;

    @Transient
    private String rawPassword;
}