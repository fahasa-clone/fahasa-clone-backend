package vn.clone.fahasa_backend.service.impl;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.Book;
import vn.clone.fahasa_backend.domain.CartItem;
import vn.clone.fahasa_backend.domain.request.UpsertCartItemRequestDTO;
import vn.clone.fahasa_backend.domain.response.CartItemDTO;
import vn.clone.fahasa_backend.domain.response.UpsertCartItemResponseDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.CartItemRepository;
import vn.clone.fahasa_backend.repository.CartItemRepositoryCustom;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.BookService;
import vn.clone.fahasa_backend.service.CartItemService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    private final CartItemRepositoryCustom cartItemRepositoryCustom;

    private final AccountService accountService;

    private final BookService bookService;

    @Override
    public UpsertCartItemResponseDTO addToCart(UpsertCartItemRequestDTO request) {
        Account account = accountService.getAccountBySecurityContext();

        Book book = bookService.findBookOrThrow(request.getBookId());

        Optional<CartItem> cartItemOptional = cartItemRepository.findByAccountIdAndBookId(account.getId(), book.getId());
        CartItem cartItem = null;
        if (cartItemOptional.isEmpty()) {
            cartItem = CartItem.builder()
                               .quantity(request.getQuantity())
                               .isClicked(request.getIsClicked())
                               .account(account)
                               .book(book)
                               .build();
        } else {
            cartItem = cartItemOptional.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        }

        if (book.getStock() < cartItem.getQuantity()) {
            throw new BadRequestException("Not enough stock for this book!");
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return convertToCartItemResponse(savedCartItem);
    }

    @Override
    public UpsertCartItemResponseDTO updateCartItem(UpsertCartItemRequestDTO request) {
        Account account = accountService.getAccountBySecurityContext();

        Book book = bookService.findBookOrThrow(request.getBookId());

        if (book.getStock() < request.getQuantity()) {
            throw new BadRequestException("Not enough stock for this book!");
        }

        CartItem existingCartItem = cartItemRepository.findByAccountIdAndBookId(account.getId(), book.getId())
                                                      .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        existingCartItem.setQuantity(request.getQuantity());
        existingCartItem.setIsClicked(request.getIsClicked());

        return convertToCartItemResponse(cartItemRepository.save(existingCartItem));
    }

    @Override
    public void deleteCartItem(int bookId) {
        Account account = accountService.getAccountBySecurityContext();

        CartItem cartItem = cartItemRepository.findByAccountIdAndBookId(account.getId(), bookId)
                                              .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        cartItemRepository.delete(cartItem);
    }

    @Override
    public List<CartItemDTO> getAllCartItems() {
        Account account = accountService.getAccountBySecurityContext();

        return cartItemRepositoryCustom.findAllCartItemByAccountId(account.getId());
    }

    @Override
    public Optional<CartItem> findByAccountIdAndBookId(int accountId, int bookId) {
        return cartItemRepository.findByAccountIdAndBookId(accountId, bookId);
    }

    private UpsertCartItemResponseDTO convertToCartItemResponse(CartItem cartItem) {
        return UpsertCartItemResponseDTO.builder()
                                        .bookId(cartItem.getBook().getId())
                                        .bookName(cartItem.getBook().getName())
                                        .quantity(cartItem.getQuantity())
                                        .isClicked(cartItem.getIsClicked())
                                        .build();
    }
}