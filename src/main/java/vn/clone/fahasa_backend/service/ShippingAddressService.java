package vn.clone.fahasa_backend.service;

import java.util.List;

import vn.clone.fahasa_backend.domain.request.ShippingAddressRequestDTO;
import vn.clone.fahasa_backend.domain.response.ShippingAddressResponseDTO;

public interface ShippingAddressService {

    ShippingAddressResponseDTO createShippingAddress(ShippingAddressRequestDTO request);

    ShippingAddressResponseDTO updateShippingAddress(ShippingAddressRequestDTO request, int id);

    void deleteShippingAddress(int id);

    List<ShippingAddressResponseDTO> getAllShippingAddress();
}
