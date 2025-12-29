package vn.clone.fahasa_backend.service.impl;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.*;
import vn.clone.fahasa_backend.domain.request.OrderRequestDTO;
import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;
import vn.clone.fahasa_backend.domain.response.FullOrderDetailDTO;
import vn.clone.fahasa_backend.domain.response.OrderSummaryDTO;
import vn.clone.fahasa_backend.error.BadRequestException;
import vn.clone.fahasa_backend.repository.OrderRepository;
import vn.clone.fahasa_backend.repository.OrderRepositoryCustom;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.CartItemService;
import vn.clone.fahasa_backend.service.OrderService;
import vn.clone.fahasa_backend.service.ShippingAddressService;
import vn.clone.fahasa_backend.util.RandomUtils;
import vn.clone.fahasa_backend.util.ShippingCalculateUtils;
import vn.clone.fahasa_backend.util.constant.OrderStatus;
import vn.clone.fahasa_backend.util.constant.PaymentMethod;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final AccountService accountService;
    private final ShippingAddressService shippingAddressService;
    private final CartItemService cartItemService;
    private final OrderRepository orderRepository;
    private final OrderRepositoryCustom orderRepositoryCustom;

    @Override
    public CreateOrderResponseDTO createOrder(OrderRequestDTO request) {

        Account account = accountService.getAccountBySecurityContext();

        ShippingAddress shippingAddress = shippingAddressService.getShippingAddressByIdAndAccountId(request.getShippingAddressId(), account.getId());

        List<CartItem> clickedCartItem = cartItemService.findAllCartItemClickedByAccount(account);

        if (clickedCartItem.isEmpty()) {
            throw new BadRequestException("No books in cart!");
        }

        // Check stock
        clickedCartItem.forEach(cartItem -> {
            if (cartItem.getQuantity() > cartItem.getBook().getStock()) {
                throw new BadRequestException(String.format("Only %d \"%s\" books left in stock!",
                                                            cartItem.getBook().getStock(),
                                                            cartItem.getBook().getName()));
            }
        });

        // Get sum quantity
        Integer totalQuantity = clickedCartItem.stream().mapToInt(CartItem::getQuantity).sum();

        // Calculate total price
        Long totalPrice = clickedCartItem.stream()
                                         .mapToLong(item -> item.getQuantity() *
                                                            (item.getBook().getPrice() - item.getBook()
                                                                                             .getDiscountAmount()))
                                         .sum();

        // Calculate shipping fee
        Long shippingFee = ShippingCalculateUtils.calculateShippingFee(shippingAddress.getProvinceName(),
                                                                       totalQuantity,
                                                                       request.getShippingMethod().getValue());

        // Check the grand total displayed to the user
        if (totalPrice + shippingFee != request.getGrandTotal()) {
            throw new BadRequestException("Grand total does not match!");
        }

        Order order = Order.builder()
                           .receiverName(shippingAddress.getReceiverName())
                           .receiverPhone(shippingAddress.getReceiverPhone())
                           .address(String.format("%s, %s, %s, %s",
                                                  shippingAddress.getDetailAddress(),
                                                  shippingAddress.getWardName(),
                                                  shippingAddress.getDistrictName(),
                                                  shippingAddress.getProvinceName()))
                           .totalQuantity(totalQuantity)
                           .subTotal(totalPrice)
                           .shippingFee(shippingFee)
                           .grandTotal(totalPrice + shippingFee)
                           .paymentMethod(request.getPaymentMethod())
                           .shippingMethod(request.getShippingMethod())
                           .publicId(UUID.randomUUID())
                           .account(account)
                           .build();

        List<OrderDetail> orderDetails = clickedCartItem.stream()
                                                        .map(item -> OrderDetail.builder()
                                                                                .quantity(item.getQuantity())
                                                                                .price(item.getBook().getPrice())
                                                                                .order(order)
                                                                                .book(item.getBook())
                                                                                .build())
                                                        .toList();

        order.setOrderDetails(orderDetails);

        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            OrderState orderState = OrderState.builder().orderStatus(OrderStatus.PENDING).order(order).build();
            order.setCurrentStatus(OrderStatus.PENDING);
            order.setOrderStates(List.of(orderState));
        } else {
            OrderState orderState = OrderState.builder().orderStatus(OrderStatus.AWAITING_PAYMENT).order(order).build();
            order.setCurrentStatus(OrderStatus.AWAITING_PAYMENT);
            order.setOrderStates(List.of(orderState));
        }

        Order savedOrder = null;
        boolean flag = false;
        while (!flag) {
            try {
                order.setOrderReference(RandomUtils.generateOrderReference());
                savedOrder = orderRepository.save(order);
                flag = true;
            } catch (DataIntegrityViolationException ignored) {
            }
        }

        cartItemService.deleteAllCartItem(clickedCartItem);

        return convertToOrderResponseDTO(savedOrder);
    }

    @Override
    public Page<OrderSummaryDTO> getAllOrderSummary(Account account, OrderStatus status, Pageable pageable) {
        return orderRepositoryCustom.findAllOrderSummary(account, status, pageable);
    }

    @Override
    public FullOrderDetailDTO getOrderDetail(UUID publicId) {
        Order order = orderRepositoryCustom.findFullOrderDetailById(publicId);
        return convertToFullOrderDetailDTO(order);
    }

    @Override
    public Order getOrderById(UUID publicId) {
        return orderRepository.findByPublicId(publicId).orElseThrow(() -> new BadRequestException("Order not found!"));
    }

    private CreateOrderResponseDTO convertToOrderResponseDTO(Order order) {
        return CreateOrderResponseDTO.builder()
                                     .receiverName(order.getReceiverName())
                                     .receiverPhone(order.getReceiverPhone())
                                     .address(order.getAddress())
                                     .subTotal(order.getSubTotal())
                                     .shippingFee(order.getShippingFee())
                                     .grandTotal(order.getGrandTotal())
                                     .paymentMethod(order.getPaymentMethod())
                                     .shippingMethod(order.getShippingMethod())
                                     .publicId(order.getPublicId().toString())
                                     .orderReference(order.getOrderReference())
                                     .currentStatus(order.getCurrentStatus().getValue())
                                     .orderDetails(order.getOrderDetails()
                                                        .stream()
                                                        .map(detail -> CreateOrderResponseDTO.OrderDetailResponseDTO.builder()
                                                                                                                    .bookId(detail.getBook()
                                                                                                                                  .getId())
                                                                                                                    .quantity(detail.getQuantity())
                                                                                                                    .price(detail.getPrice())
                                                                                                                    .build())
                                                        .toList())
                                     .orderStates(order.getOrderStates()
                                                       .stream()
                                                       .map(state -> CreateOrderResponseDTO.OrderStateResponseDTO.builder()
                                                                                                                 .createdAt(state.getCreatedAt())
                                                                                                                 .status(state.getOrderStatus())
                                                                                                                 .build())
                                                       .toList())
                                     .build();
    }

    private FullOrderDetailDTO convertToFullOrderDetailDTO(Order order) {
        return FullOrderDetailDTO.builder()
                                 .receiverName(order.getReceiverName())
                                 .receiverPhone(order.getReceiverPhone())
                                 .address(order.getAddress())
                                 .subTotal(order.getSubTotal())
                                 .shippingFee(order.getShippingFee())
                                 .grandTotal(order.getGrandTotal())
                                 .paymentMethod(order.getPaymentMethod())
                                 .shippingMethod(order.getShippingMethod())
                                 .publicId(order.getPublicId().toString())
                                 .orderReference(order.getOrderReference())
                                 .currentStatus(order.getCurrentStatus().getValue())
                                 .orderDetails(order.getOrderDetails()
                                                    .stream()
                                                    .map(detail -> FullOrderDetailDTO.OrderDetailResponseDTO.builder()
                                                                                                            .bookId(detail.getBook()
                                                                                                                          .getId())
                                                                                                            .quantity(detail.getQuantity())
                                                                                                            .price(detail.getPrice())
                                                                                                            .bookName(detail.getBook()
                                                                                                                            .getName())
                                                                                                            // .bookImage(detail.getBook()
                                                                                                            //                  .getBookImages()
                                                                                                            //                  .stream()
                                                                                                            //                  .findFirst()
                                                                                                            //                  .map(BookImage::getImagePath)
                                                                                                            //                  .orElse(""))
                                                                                                            .build())
                                                    .toList())
                                 .orderStates(order.getOrderStates()
                                                   .stream()
                                                   .map(state -> FullOrderDetailDTO.OrderStateResponseDTO.builder()
                                                                                                         .createdAt(state.getCreatedAt())
                                                                                                         .status(state.getOrderStatus())
                                                                                                         .build())
                                                   .toList())
                                 .build();
    }
}
