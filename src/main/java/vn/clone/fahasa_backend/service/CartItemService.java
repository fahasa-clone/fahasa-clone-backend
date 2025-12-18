package vn.clone.fahasa_backend.service;

import java.util.List;
import java.util.Optional;

import vn.clone.fahasa_backend.domain.CartItem;
import vn.clone.fahasa_backend.domain.request.UpsertCartItemRequestDTO;
import vn.clone.fahasa_backend.domain.response.CartItemDTO;
import vn.clone.fahasa_backend.domain.response.UpsertCartItemResponseDTO;

public interface CartItemService {

    UpsertCartItemResponseDTO addToCart(UpsertCartItemRequestDTO request);

    UpsertCartItemResponseDTO updateCartItem(UpsertCartItemRequestDTO request);

    void deleteCartItem(int bookId);

    List<CartItemDTO> getAllCartItems();

    Optional<CartItem> findByAccountIdAndBookId(int accountId, int bookId);

}
