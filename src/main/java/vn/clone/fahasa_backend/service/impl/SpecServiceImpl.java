package vn.clone.fahasa_backend.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Spec;
import vn.clone.fahasa_backend.domain.request.SpecRequestDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.SpecRepository;
import vn.clone.fahasa_backend.service.SpecService;

@Service
@RequiredArgsConstructor
public class SpecServiceImpl implements SpecService {

    private final SpecRepository specRepository;

    @Override
    @Transactional
    public Spec createSpec(SpecRequestDTO requestDTO) {
        validateSpecNameIsUnique(requestDTO.getName());

        Spec newSpec = Spec.builder()
                           .name(requestDTO.getName())
                           .isFiltered(requestDTO.isFiltered())
                           .build();

        return specRepository.save(newSpec);
    }

    @Override
    @Transactional(readOnly = true)
    public Spec getSpecById(Integer id) {
        return specRepository.findById(id)
                             .orElseThrow(() -> new EntityNotFoundException("Specification not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Spec> getAllSpecs(Pageable pageable) {
        return specRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Spec updateSpec(Integer id, SpecRequestDTO requestDTO) {
        Spec existingSpec = getSpecById(id);

        if (!existingSpec.getName()
                         .equals(requestDTO.getName())) {
            validateSpecNameIsUnique(requestDTO.getName());
            existingSpec.setName(requestDTO.getName());
        }

        existingSpec.setFiltered(requestDTO.isFiltered());

        return specRepository.save(existingSpec);
    }

    @Override
    @Transactional
    public void deleteSpec(Integer id) {
        Spec spec = getSpecById(id);

        specRepository.delete(spec);
    }

    private void validateSpecNameIsUnique(String name) {
        specRepository.findByName(name)
                      .ifPresent(spec -> {
                          throw new BadRequestException("Specification with this name already exists");
                      });
    }
}
