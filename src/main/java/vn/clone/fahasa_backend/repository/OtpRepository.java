package vn.clone.fahasa_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Otp;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Integer> {

    @Modifying // Tells Spring this is an UPDATE or DELETE
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Required for @Modifying queries
    @Query("DELETE FROM Otp o WHERE o.accountId = :accountId")
    void deleteByAccountId(@Param("accountId") int accountId);

    Optional<Otp> findByAccountId(Integer accountId);
}
