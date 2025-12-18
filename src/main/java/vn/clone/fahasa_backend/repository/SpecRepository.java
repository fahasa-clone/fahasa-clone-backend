package vn.clone.fahasa_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Spec;

@Repository
public interface SpecRepository extends JpaRepository<Spec, Integer> {
    
    Optional<Spec> findByName(String name);
}
