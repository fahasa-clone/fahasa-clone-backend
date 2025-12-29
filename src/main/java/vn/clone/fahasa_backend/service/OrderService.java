package vn.clone.fahasa_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.Order;
import vn.clone.fahasa_backend.domain.request.OrderRequestDTO;
import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;
import vn.clone.fahasa_backend.domain.response.FullOrderDetailDTO;
import vn.clone.fahasa_backend.domain.response.OrderSummaryDTO;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

@Service
public interface OrderService {

    CreateOrderResponseDTO createOrder(OrderRequestDTO request);

    Page<OrderSummaryDTO> getAllOrderSummary(Account account, OrderStatus status, Pageable pageable);

    FullOrderDetailDTO getOrderDetail(UUID publicId);

    Order getOrderById(UUID publicId);
}
