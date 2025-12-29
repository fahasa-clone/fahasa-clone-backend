package vn.clone.fahasa_backend.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.ShippingAddress;
import vn.clone.fahasa_backend.domain.response.CartItemResponseDTO;
import vn.clone.fahasa_backend.domain.response.CheckoutResponseDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.CartItemService;
import vn.clone.fahasa_backend.service.CheckoutService;
import vn.clone.fahasa_backend.service.ShippingAddressService;
import vn.clone.fahasa_backend.util.ShippingCalculateUtils;
import vn.clone.fahasa_backend.util.constant.ShippingMethod;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final AccountService accountService;

    private final ShippingAddressService shippingAddressService;

    private final CartItemService cartItemService;


    @Override
    public CheckoutResponseDTO checkout(Integer addressId, ShippingMethod shippingMethod) {
        Account account = accountService.getAccountBySecurityContext();

        ShippingAddress shippingAddress = shippingAddressService.getShippingAddressByIdAndAccountId(addressId, account.getId());

        List<CartItemResponseDTO> cartItems = cartItemService.getAllCartItems(account)
                                                             .stream()
                                                             .filter(CartItemResponseDTO::getIsClicked)
                                                             .toList();

        if (cartItems.isEmpty()) {
            throw new BadRequestException("No books clicked in cart!");
        }

        cartItems.forEach(cartItem -> {
            if (cartItem.getQuantity() > cartItem.getBookStock()) {
                throw new BadRequestException(String.format("Only %d \"%s\" books left in stock!",
                                                            cartItem.getBookStock(),
                                                            cartItem.getBookName()));
            }
        });

        Long subTotal = cartItems.stream()
                                 .mapToLong(item -> item.getQuantity() * (item.getBookPrice() - item.getBookDiscountAmount()))
                                 .sum();

        Integer totalQuantity = cartItems.stream().mapToInt(CartItemResponseDTO::getQuantity).sum();
        Long shippingFee = ShippingCalculateUtils.calculateShippingFee(shippingAddress.getProvinceName(), totalQuantity, shippingMethod.getValue());

        Long grandTotal = subTotal + shippingFee;

        return CheckoutResponseDTO.builder()
                                  .cartItems(cartItems)
                                  .subTotal(subTotal)
                                  .shippingFee(shippingFee)
                                  .grandTotal(grandTotal)
                                  .build();
    }


}