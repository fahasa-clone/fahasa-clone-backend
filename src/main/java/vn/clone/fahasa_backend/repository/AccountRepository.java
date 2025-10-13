package vn.clone.fahasa_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    public boolean existsByEmail(String email);

    public Optional<Account> findByEmail(String email);

    public Optional<Account> findByEmailAndActivationKey(String email, String activationKey);

    public Optional<Account> findByEmailAndIsActivated(String email, boolean isActivated);

    public Optional<Account> findByEmailAndToken(String email, String token);
}
