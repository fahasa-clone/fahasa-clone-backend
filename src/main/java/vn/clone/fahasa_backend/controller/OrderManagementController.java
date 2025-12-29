package vn.clone.fahasa_backend.controller;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.response.FullOrderDetailDTO;
import vn.clone.fahasa_backend.service.OrderManagementService;
import vn.clone.fahasa_backend.service.OrderService;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderManagementController {

    private final OrderService orderService;
    private final OrderManagementService orderManagementService;

    @PutMapping("/confirm/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> confirmOrder(@PathVariable String publicId) {
        UUID orderId = UUID.fromString(publicId);
        System.out.println(orderId);
        orderManagementService.confirmOrder(orderId);
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @PutMapping("/process/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> processOrder(@PathVariable String publicId) {
        UUID orderId = UUID.fromString(publicId);
        orderManagementService.processOrder(orderId);
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @PutMapping("/shipping/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> shipOrder(@PathVariable String publicId) {
        UUID orderId = UUID.fromString(publicId);
        orderManagementService.shipOrder(orderId);
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @PutMapping("/complete/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> completeOrder(@PathVariable String publicId) {
        UUID orderId = UUID.fromString(publicId);
        orderManagementService.completeOrder(orderId);
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @PutMapping("/cancel/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> cancelOrder(@PathVariable String publicId) {
        UUID orderId = UUID.fromString(publicId);
        orderManagementService.cancelOrder(orderId);
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }
}
