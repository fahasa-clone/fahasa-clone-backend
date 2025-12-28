package vn.clone.fahasa_backend.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.request.OrderRequestDTO;
import vn.clone.fahasa_backend.domain.response.CheckoutResponseDTO;
import vn.clone.fahasa_backend.domain.response.CreateOrderResponseDTO;
import vn.clone.fahasa_backend.domain.response.OrderSummaryDTO;
import vn.clone.fahasa_backend.util.constant.OrderStatus;
import vn.clone.fahasa_backend.util.constant.ShippingMethod;

public interface CheckoutService {

    CheckoutResponseDTO checkout(Integer addressId, ShippingMethod shippingMethod);
    
}
