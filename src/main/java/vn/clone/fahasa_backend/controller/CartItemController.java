package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.request.CartItemRequestDTO;
import vn.clone.fahasa_backend.domain.response.CartItemResponseDTO;
import vn.clone.fahasa_backend.domain.response.UpsertCartItemResponseDTO;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.CartItemService;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<UpsertCartItemResponseDTO> addToCart(@Valid @RequestBody CartItemRequestDTO request) {
        UpsertCartItemResponseDTO newCartItem = cartItemService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(newCartItem);
    }

    @PutMapping
    public ResponseEntity<UpsertCartItemResponseDTO> updateCart(@Valid @RequestBody CartItemRequestDTO request) {
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
    public ResponseEntity<List<CartItemResponseDTO>> getAllCartItem() {
        Account account = accountService.getAccountBySecurityContext();

        List<CartItemResponseDTO> cartItems = cartItemService.getAllCartItems(account);
        return ResponseEntity.ok(cartItems);
    }

    // @GetMapping("/clicked")
    // public ResponseEntity<List<CartItemResponseDTO>> getAllCartItemClicked() {
    //     Account account = accountService.getAccountBySecurityContext();
    //     List<CartItemResponseDTO> cartItems = cartItemService.getAllCartItemsClicked1(account);
    //     return ResponseEntity.ok(cartItems);
    // }
    //
    // @GetMapping("/test")
    // public ResponseEntity<Object> test() {
    //     return ResponseEntity.ok(cartItemService.getAllCartItemsClicked(accountService.getAccountBySecurityContext()));
    // }


}
