package vn.clone.fahasa_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
}
