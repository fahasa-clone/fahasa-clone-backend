package vn.clone.fahasa_backend.service.impl;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.Account;
import vn.clone.fahasa_backend.domain.ShippingAddress;
import vn.clone.fahasa_backend.domain.Ward;
import vn.clone.fahasa_backend.domain.request.ShippingAddressRequestDTO;
import vn.clone.fahasa_backend.domain.response.ShippingAddressResponseDTO;
import vn.clone.fahasa_backend.repository.ShippingAddressRepository;
import vn.clone.fahasa_backend.repository.ShippingAddressRepositoryCustom;
import vn.clone.fahasa_backend.service.AccountService;
import vn.clone.fahasa_backend.service.LocationService;
import vn.clone.fahasa_backend.service.ShippingAddressService;
import vn.clone.fahasa_backend.util.SecurityUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;

    private final ShippingAddressRepositoryCustom shippingAddressRepositoryCustom;

    private final AccountService accountService;

    private final LocationService locationService;

    @Override
    public ShippingAddressResponseDTO createShippingAddress(ShippingAddressRequestDTO request) {
        String email = SecurityUtils.getCurrentUserLogin()
                                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Account account = accountService.getUserInfo(email);

        Ward ward = locationService.getWardById(request.getWardId());

        ShippingAddress newAddress = ShippingAddress.builder()
                                                    .account(account)
                                                    .ward(ward)
                                                    .receiverName(request.getReceiverName())
                                                    .receiverPhone(request.getReceiverPhone())
                                                    .detailAddress(request.getDetailAddress())
                                                    .wardName(ward.getName())
                                                    .districtName(ward.getDistrict().getName())
                                                    .provinceName(ward.getDistrict().getProvince().getName())
                                                    .isDefault(request.getIsDefault())
                                                    .build();

        if (request.getIsDefault()) {
            enableDefaultAddress(account);
        }

        ShippingAddress savedAddress = shippingAddressRepository.save(newAddress);

        return convertToShippingAddressResponse(savedAddress);
    }

    @Override
    public ShippingAddressResponseDTO updateShippingAddress(ShippingAddressRequestDTO request, int id) {
        String email = SecurityUtils.getCurrentUserLogin()
                                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Account account = accountService.getUserInfo(email);

        ShippingAddress address = shippingAddressRepository.findByIdAndAccountId(id, account.getId())
                                                           .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        Ward ward = locationService.getWardById(request.getWardId());

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setIsDefault(request.getIsDefault());
        address.setWard(ward);
        address.setDetailAddress(request.getDetailAddress());
        address.setWardName(ward.getName());
        address.setDistrictName(ward.getDistrict().getName());
        address.setProvinceName(ward.getDistrict().getProvince().getName());

        if (request.getIsDefault()) {
            enableDefaultAddress(account);
        }

        ShippingAddress updatedAddress = shippingAddressRepository.save(address);
        return convertToShippingAddressResponse(updatedAddress);
    }

    @Override
    public void deleteShippingAddress(int id) {
        String email = SecurityUtils.getCurrentUserLogin()
                                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Account account = accountService.getUserInfo(email);

        ShippingAddress address = shippingAddressRepository.findByIdAndAccountId(id, account.getId())
                                                           .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        shippingAddressRepository.delete(address);
    }

    @Override
    public List<ShippingAddressResponseDTO> getAllShippingAddress() {
        String email = SecurityUtils.getCurrentUserLogin()
                                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Account account = accountService.getUserInfo(email);

        return shippingAddressRepositoryCustom.findAllAddressesByAccountId(account.getId());
    }

    private void enableDefaultAddress(Account account) {
        Optional<ShippingAddress> addressOptional = shippingAddressRepository.findByAccountIdAndIsDefaultIsTrue(account.getId());
        if (addressOptional.isPresent()) {
            ShippingAddress address = addressOptional.get();
            address.setIsDefault(false);
            shippingAddressRepository.save(address);
        }
    }

    private ShippingAddressResponseDTO convertToShippingAddressResponse(ShippingAddress address) {
        return ShippingAddressResponseDTO.builder()
                                         .id(address.getId())
                                         .wardId(address.getWard().getId())
                                         .receiverName(address.getReceiverName())
                                         .receiverPhone(address.getReceiverPhone())
                                         .detailAddress(address.getDetailAddress())
                                         .wardName(address.getWard().getName())
                                         .districtName(address.getWard().getDistrict().getName())
                                         .provinceName(address.getWard().getDistrict().getProvince().getName())
                                         .isDefault(address.getIsDefault())
                                         .build();
    }
}
