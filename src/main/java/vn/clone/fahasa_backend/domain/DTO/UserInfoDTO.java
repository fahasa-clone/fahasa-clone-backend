package vn.clone.fahasa_backend.domain.DTO;

import java.sql.Date;

import lombok.Setter;

@Setter
public class UserInfoDTO {
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Date birthday;
}
