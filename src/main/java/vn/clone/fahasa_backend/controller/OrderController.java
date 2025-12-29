package vn.clone.fahasa_backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.request.OrderRequestDTO;
import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;
import vn.clone.fahasa_backend.domain.response.FullOrderDetailDTO;
import vn.clone.fahasa_backend.domain.response.OrderSummaryDTO;
import vn.clone.fahasa_backend.domain.response.PageResponse;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.CheckoutService;
import vn.clone.fahasa_backend.service.OrderService;
import vn.clone.fahasa_backend.util.SecurityUtils;
import vn.clone.fahasa_backend.util.constant.OrderStatus;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final AccountService accountService;

    @PostMapping()
    public ResponseEntity<CreateOrderResponseDTO> createOrder(@RequestBody @Valid OrderRequestDTO request) {
        CreateOrderResponseDTO newOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(newOrder);
    }

    @GetMapping()
    public ResponseEntity<PageResponse<List<OrderSummaryDTO>>> getAllOrders(Pageable pageable,
                                                                            @RequestParam(value = "status") Optional<String> statusOtp) {
        OrderStatus status = null;
        if (statusOtp.isPresent()) {
            try {
                status = OrderStatus.valueOf(statusOtp.get());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid order status!");
            }
        }

        Account account = accountService.getAccountBySecurityContext();

        Page<OrderSummaryDTO> orderPage = orderService.getAllOrderSummary(account, status, pageable);
        PageResponse<List<OrderSummaryDTO>> pageResponse = new PageResponse<>(pageable.getPageNumber() + 1, pageable.getPageSize(),
                                                                              orderPage.getTotalPages(), orderPage.getContent());
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<FullOrderDetailDTO> getOrderById(@PathVariable String publicId) {
        UUID orderId = UUID.fromString(publicId);
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

}
