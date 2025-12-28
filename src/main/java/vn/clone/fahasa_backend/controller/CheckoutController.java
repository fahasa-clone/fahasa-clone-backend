package vn.clone.fahasa_backend.controller;

import java.util.Optional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.request.OrderRequestDTO;
import vn.clone.fahasa_backend.domain.response.CheckoutResponseDTO;
import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.CheckoutService;
import vn.clone.fahasa_backend.util.constant.ShippingMethod;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;
    
    @GetMapping()
    public ResponseEntity<CheckoutResponseDTO> getCheckoutSummary(@RequestParam(name = "addressId") Optional<String> addressIdOtp,
                                                                  @RequestParam(name = "shippingMethod") Optional<String> shippingMethodOtp) {
        if (addressIdOtp.isEmpty()) {
            throw new BadRequestException("Address id is required!");
        }
        if (shippingMethodOtp.isEmpty()) {
            throw new BadRequestException("Shipping method is required!");
        }

        try {
            Integer addressId = Integer.parseInt(addressIdOtp.get());
            ShippingMethod shippingMethod = ShippingMethod.valueOf(shippingMethodOtp.get());
            return ResponseEntity.ok(checkoutService.checkout(addressId, shippingMethod));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid address id!");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid shipping method!");
        }
    }
}
