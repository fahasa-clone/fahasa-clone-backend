package vn.clone.fahasa_backend.controller;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.response.FullOrderDetailDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.OrderManagementService;
import vn.clone.fahasa_backend.service.OrderService;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderManagementController {

    private final OrderService orderService;
    private final OrderManagementService orderManagementService;

    @PutMapping("/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> updateOrderStatus(@PathVariable String publicId, @RequestParam Optional<String> status) {
        UUID orderId = UUID.fromString(publicId);

        if (status.isEmpty()) {
            throw new BadRequestException("Status is required!");
        }

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.get());
            orderManagementService.updateStatusOfOrder(orderId, orderStatus);
            return ResponseEntity.ok(orderService.getOrderDetail(orderId));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status!");
        }
    }
}
