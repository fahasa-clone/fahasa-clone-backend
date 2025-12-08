package vn.clone.fahasa_backend.service.impl;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.clone.fahasa_backend.domain.Publisher;
import vn.clone.fahasa_backend.domain.request.PublisherRequestDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.PublisherRepository;
import vn.clone.fahasa_backend.service.PublisherService;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    @Transactional
    public Publisher createPublisher(PublisherRequestDTO requestDTO) {
        checkAuthorNameExists(requestDTO.getName());

        Publisher newPublisher = Publisher.builder()
                                          .name(requestDTO.getName())
                                          .build();

        return publisherRepository.save(newPublisher);
    }

    @Override
    @Transactional(readOnly = true)
    public Publisher getPublisherById(Integer id) {
        return publisherRepository.findById(id)
                                  .orElseThrow(() -> new EntityNotFoundException("Publisher not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Publisher> getAllPublishers(Pageable pageable) {
        return publisherRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Publisher updatePublisher(Integer id, PublisherRequestDTO requestDTO) {
        Publisher existingPublisher = getPublisherById(id);

        checkAuthorNameExists(requestDTO.getName());

        existingPublisher.setName(requestDTO.getName());

        return publisherRepository.save(existingPublisher);
    }

    @Override
    @Transactional
    public void deletePublisher(Integer id) {
        Publisher publisher = getPublisherById(id);

        publisherRepository.delete(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Publisher> getPublisherByName(String name) {
        return publisherRepository.findByName(name);
    }

    private void checkAuthorNameExists(String name) {
        publisherRepository.findByName(name)
                           .ifPresent(author -> {
                               throw new BadRequestException("Publisher with this name already exists");
                           });
    }
}
