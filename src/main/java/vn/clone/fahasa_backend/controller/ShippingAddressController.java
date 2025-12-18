package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.request.ShippingAddressRequestDTO;
import vn.clone.fahasa_backend.domain.request.UpsertCartItemRequestDTO;
import vn.clone.fahasa_backend.domain.response.CartItemDTO;
import vn.clone.fahasa_backend.domain.response.ShippingAddressResponseDTO;
import vn.clone.fahasa_backend.domain.response.UpsertCartItemResponseDTO;
import vn.clone.fahasa_backend.service.ShippingAddressService;

@RestController
@RequestMapping("/api/shipping-addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @PostMapping
    public ResponseEntity<ShippingAddressResponseDTO> createShippingAddress(@Valid @RequestBody ShippingAddressRequestDTO request) {
        ShippingAddressResponseDTO newShippingAddress = shippingAddressService.createShippingAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(newShippingAddress);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingAddressResponseDTO> updateShippingAddress(@PathVariable Integer id,
                                                                            @Valid @RequestBody ShippingAddressRequestDTO request) {
        ShippingAddressResponseDTO updatedShippingAddress = shippingAddressService.updateShippingAddress(request, id);
        return ResponseEntity.ok(updatedShippingAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShippingAddress(@PathVariable Integer id) {
        shippingAddressService.deleteShippingAddress(id);
        return ResponseEntity.noContent()
                             .build();
    }

    @GetMapping
    public ResponseEntity<List<ShippingAddressResponseDTO>> getAllShippingAddress() {
        List<ShippingAddressResponseDTO> addresses = shippingAddressService.getAllShippingAddress();
        return ResponseEntity.ok(addresses);
    }
}
