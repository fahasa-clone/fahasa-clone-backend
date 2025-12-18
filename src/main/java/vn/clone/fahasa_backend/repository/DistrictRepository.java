package vn.clone.fahasa_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.clone.fahasa_backend.domain.District;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findByProvinceId(Integer provinceId);
}
