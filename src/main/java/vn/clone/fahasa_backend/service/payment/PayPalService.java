package vn.clone.fahasa_backend.service.payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.controllers.OrdersController;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.request.CreateOrderRequest;
import vn.clone.fahasa_backend.domain.request.PayPalCartItem;
import vn.clone.fahasa_backend.error.PayPalException;
import vn.clone.fahasa_backend.service.LiveCurrencyService;

@Service
@Slf4j
public class PayPalService {

    private static final String CURRENCY = "USD";

    private final OrdersController ordersController;

    private final LiveCurrencyService liveCurrencyService;

    public PayPalService(PaypalServerSdkClient client, LiveCurrencyService liveCurrencyService) {
        this.ordersController = client.getOrdersController();
        this.liveCurrencyService = liveCurrencyService;
    }

    public Order createOrder(CreateOrderRequest request) {
        if (request == null || request.getCart() == null || request.getCart().isEmpty()) {
            log.warn("Cart is empty or null");
            throw new PayPalException("Cart cannot be empty");
        }

        try {
            BigDecimal totalAmount = calculateCartTotal(request).add(request.getShippingFee());
            log.info("Creating PayPal order with amount: {}", totalAmount);

            CreateOrderInput createOrderInput = buildCreateOrderInput(request);

            ApiResponse<Order> apiResponse = ordersController.createOrder(createOrderInput);
            Order order = apiResponse.getResult();

            log.info("Order created successfully with ID: {}", order.getId());
            return order;

        } catch (ApiException e) {
            log.error("PayPal API error while creating order", e);
            throw new PayPalException("Failed to create order: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error while creating order", e);
            throw new PayPalException("IO error while creating order", e);
        }
    }

    public Order captureOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            log.warn("Order ID is null or empty");
            throw new PayPalException("Order ID cannot be empty");
        }

        try {
            log.info("Capturing order with ID: {}", orderId);

            CaptureOrderInput captureOrderInput = new CaptureOrderInput.Builder()
                    .id(orderId)
                    .build();
            ApiResponse<Order> apiResponse = ordersController.captureOrder(captureOrderInput);
            Order order = apiResponse.getResult();

            log.info("Order {} captured successfully", orderId);
            return order;

        } catch (ApiException e) {
            log.error("PayPal API error while capturing order: {}", orderId, e);
            throw new PayPalException("Failed to capture order: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error while capturing order: {}", orderId, e);
            throw new PayPalException("IO error while capturing order", e);
        }
    }

    private BigDecimal calculateCartTotal(CreateOrderRequest request) {
        return request.getCart()
                      .stream()
                      .map(PayPalCartItem::getTotal)
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CreateOrderInput buildCreateOrderInput(CreateOrderRequest request) {
        BigDecimal rate = liveCurrencyService.getVndToUsdRate();

        BigDecimal itemTotal = request.getTotalPrice().subtract(request.getShippingFee());

        AmountBreakdown breakdown = new AmountBreakdown.Builder()
                .shipping(new Money(CURRENCY, request.getShippingFee()
                                                     .multiply(rate)
                                                     .toString()))
                .itemTotal(new Money(CURRENCY, itemTotal.multiply(rate)
                                                        .toString()))
                .build();

        AmountWithBreakdown amountWithBreakdown = new AmountWithBreakdown.Builder()
                .currencyCode(CURRENCY)
                .value(request.getTotalPrice()
                              .toString())
                .breakdown(breakdown)
                .build();

        List<ItemRequest> items = request.getCart()
                                         .stream()
                                         .map(item -> new ItemRequest.Builder()
                                                 .name(item.getName())
                                                 .quantity(item.getQuantity()
                                                               .toString())
                                                 .unitAmount(new Money(CURRENCY, item.getPrice()
                                                                                     .multiply(rate)
                                                                                     .toString()))
                                                 .build())
                                         .toList();

        return new CreateOrderInput.Builder()
                .body(new OrderRequest.Builder()
                              .intent(CheckoutPaymentIntent.CAPTURE)
                              .purchaseUnits(Arrays.asList(
                                      new PurchaseUnitRequest.Builder()
                                              .amount(amountWithBreakdown)
                                              .items(items)
                                              .build()))
                              .build())
                .build();
    }
}
