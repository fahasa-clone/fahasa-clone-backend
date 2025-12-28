package vn.clone.fahasa_backend.service;

import java.util.UUID;

import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;

public interface OrderManagementService {
    void confirmOrder(UUID publicId);

    void processOrder(UUID publicId);

    void shipOrder(UUID publicId);

    void completeOrder(UUID publicId);

    void returnOrder(UUID publicId);

    void cancelOrder(UUID publicId);

}
