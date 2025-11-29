package vn.clone.fahasa_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailIgnoreCase(String email);

    Optional<Account> findByEmailAndActivationKey(String email, String activationKey);

    Optional<Account> findByEmailAndIsActivated(String email, boolean isActivated);
}
