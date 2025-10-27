package vn.clone.fahasa_backend.configuration;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import vn.clone.fahasa_backend.domain.DTO.UserDTO;

public class CustomUser extends User {
    private UserDTO userDto;

    public CustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities, UserDTO userDto) {
        super(username, password, authorities);
        this.userDto = userDto;
    }

    public UserDTO getUserInfo() {
        return userDto;
    }

}