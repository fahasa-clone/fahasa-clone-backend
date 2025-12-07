package vn.clone.fahasa_backend.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.clone.fahasa_backend.domain.Publisher;
import vn.clone.fahasa_backend.domain.request.PublisherRequestDTO;

public interface PublisherService {

    Publisher createPublisher(PublisherRequestDTO requestDTO);

    Publisher getPublisherById(Integer id);

    Page<Publisher> getAllPublishers(Pageable pageable);

    Publisher updatePublisher(Integer id, PublisherRequestDTO requestDTO);

    void deletePublisher(Integer id);

    Optional<Publisher> getPublisherByName(String name);
}
