package vn.clone.fahasa_backend.configuration;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.DTO.UserDTO;
import vn.clone.fahasa_backend.error.InvalidAccountException;
import vn.clone.fahasa_backend.repository.AccountRepository;

@Component("userDetailsService")
public class UserDetailServiceCustom implements UserDetailsService {
    private final AccountRepository accountRepository;

    public UserDetailServiceCustom(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountOptional = accountRepository.findByEmailAndIsActivated(username, true);
        if (accountOptional.isEmpty()) {
            throw new InvalidAccountException("User not found or not activated!");
        }
        Account account = accountOptional.get();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(account.getId());
        userDTO.setEmail(account.getEmail());
        userDTO.setFirstName(account.getFirstName());
        userDTO.setLastName(account.getLastName());
        return new CustomUser(account.getEmail(), account.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), userDTO);
    }
}