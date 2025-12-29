package vn.clone.fahasa_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.clone.fahasa_backend.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPublicId(UUID publicId);
}
