package vn.clone.fahasa_backend.service;

import java.util.UUID;

import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

public interface OrderManagementService {

    void updateStatusOfOrder(UUID publicId, OrderStatus status);

}
