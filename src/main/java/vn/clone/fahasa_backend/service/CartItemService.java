package vn.clone.fahasa_backend.service;

import java.util.List;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.CartItem;
import vn.clone.fahasa_backend.domain.request.CartItemRequestDTO;
import vn.clone.fahasa_backend.domain.response.CartItemResponseDTO;
import vn.clone.fahasa_backend.domain.response.UpsertCartItemResponseDTO;

public interface CartItemService {

    UpsertCartItemResponseDTO addToCart(CartItemRequestDTO request);

    UpsertCartItemResponseDTO updateCartItem(CartItemRequestDTO request);

    void deleteCartItem(int bookId);

    List<CartItemResponseDTO> getAllCartItems(Account account);

    List<CartItem> findAllCartItemClickedByAccount(Account account);

    void deleteAllCartItem(List<CartItem> cartItems);

}