package vn.clone.fahasa_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String refreshToken);

    void deleteByToken(String refreshToken);

    @Modifying
    @Query(value = "DELETE FROM refresh_tokens r WHERE r.account_id = :accountId", nativeQuery = true)
    void deleteAllByAccountId(Integer accountId);
}
