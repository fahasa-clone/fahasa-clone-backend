package vn.clone.fahasa_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.ShippingAddress;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Integer> {
    Optional<ShippingAddress> findByIdAndAccountId(Integer id, Integer accountId);

    Optional<ShippingAddress> findByAccountIdAndIsDefaultIsTrue(Integer accountId);
}
