package vn.clone.fahasa_backend.domain.response;

import java.time.LocalDate;

import lombok.*;

import vn.clone.fahasa_backend.util.constant.Gender;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Gender gender;
    private LocalDate birthday;
    private boolean isActivated;
}
