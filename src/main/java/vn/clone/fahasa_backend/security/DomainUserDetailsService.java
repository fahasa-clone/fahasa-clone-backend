package vn.clone.fahasa_backend.security;

import java.util.Collection;
import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.error.UserNotActivatedException;
import vn.clone.fahasa_backend.repository.AccountRepository;

/**
 * Authenticate a user from the database.
 */
@Component
@AllArgsConstructor
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String email) {
        LOG.debug("Authenticating {}", email);

        return accountRepository.findByEmailIgnoreCase(email)
                                .map(this::createSpringSecurityUser)
                                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " was not found in the database"));
    }

    private User createSpringSecurityUser(Account account) {
        if (!account.isActivated()) {
            throw new UserNotActivatedException("Account " + account.getEmail() + " was not activated");
        }
        return UserWithId.fromAccount(account);
    }

    @Getter
    public static class UserWithId extends User {

        private final int id;

        public UserWithId(String email, String password, Collection<? extends GrantedAuthority> authorities, int id) {
            super(email, password, authorities);
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public static UserWithId fromAccount(Account account) {
            return new UserWithId(account.getEmail(),
                                  account.getPassword(),
                                  Collections.singleton(new SimpleGrantedAuthority(account.getRole()
                                                                                          .getName())),
                                  account.getId()
            );
        }
    }
}
