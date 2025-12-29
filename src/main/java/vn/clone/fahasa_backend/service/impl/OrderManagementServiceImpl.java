package vn.clone.fahasa_backend.service.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Order;
import vn.clone.fahasa_backend.domain.OrderState;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.OrderRepository;
import vn.clone.fahasa_backend.service.OrderManagementService;
import vn.clone.fahasa_backend.service.OrderService;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderManagementServiceImpl implements OrderManagementService {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public void updateStatusOfOrder(UUID publicId, OrderStatus status) {
        Order order = orderService.getOrderById(publicId);
        if (order.getCurrentStatus().canTransitionTo(status)) {
            OrderState orderState = OrderState.builder()
                                              .orderStatus(status)
                                              .order(order)
                                              .build();
            order.getOrderStates().add(orderState);
            order.setCurrentStatus(status);
            orderRepository.save(order);
        } else {
            throw new BadRequestException("Order cannot transition to this status!");
        }
    }
}
