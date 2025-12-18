package vn.clone.fahasa_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.clone.fahasa_backend.domain.Spec;
import vn.clone.fahasa_backend.domain.request.SpecRequestDTO;

public interface SpecService {

    Spec createSpec(SpecRequestDTO requestDTO);

    Spec getSpecById(Integer id);

    Page<Spec> getAllSpecs(Pageable pageable);

    Spec updateSpec(Integer id, SpecRequestDTO requestDTO);

    void deleteSpec(Integer id);
}
