package vn.clone.fahasa_backend.configuration;

import java.util.Collection;
import java.util.List;


import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import vn.clone.fahasa_backend.domain.DTO.UserDTO;

public class CustomUser extends org.springframework.security.core.userdetails.User {
    private UserDTO userDto;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, UserDTO userDto) {
        super(username, password, authorities);
        this.userDto = userDto;
    }

    public UserDTO getUserInfo() {
        return userDto;
    }

}