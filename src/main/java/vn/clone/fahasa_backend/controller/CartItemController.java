package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.request.UpsertCartItemRequestDTO;
import vn.clone.fahasa_backend.domain.response.CartItemDTO;
import vn.clone.fahasa_backend.domain.response.UpsertCartItemResponseDTO;
import vn.clone.fahasa_backend.service.CartItemService;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<UpsertCartItemResponseDTO> addToCart(@Valid @RequestBody UpsertCartItemRequestDTO request) {
        UpsertCartItemResponseDTO newCartItem = cartItemService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(newCartItem);
    }

    @PutMapping
    public ResponseEntity<UpsertCartItemResponseDTO> updateCart(@Valid @RequestBody UpsertCartItemRequestDTO request) {
        UpsertCartItemResponseDTO updatedCartItem = cartItemService.updateCartItem(request);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/{book_id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable(name = "book_id") Integer bookId) {
        cartItemService.deleteCartItem(bookId);
        return ResponseEntity.noContent()
                             .build();
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getAllCartItem() {
        List<CartItemDTO> cartItems = cartItemService.getAllCartItems();
        return ResponseEntity.ok(cartItems);
    }
}
