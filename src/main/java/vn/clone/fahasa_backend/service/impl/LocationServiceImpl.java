package vn.clone.fahasa_backend.service.impl;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import vn.clone.fahasa_backend.domain.District;
import vn.clone.fahasa_backend.domain.Province;
import vn.clone.fahasa_backend.domain.Ward;
import vn.clone.fahasa_backend.repository.DistrictRepository;
import vn.clone.fahasa_backend.repository.ProvinceRepository;
import vn.clone.fahasa_backend.repository.WardRepository;
import vn.clone.fahasa_backend.service.LocationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final ProvinceRepository provinceRepository;

    private final DistrictRepository districtRepository;

    private final WardRepository wardRepository;

    @Override
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    @Override
    public List<District> getDistrictsByProvince(int provinceId) {
        return districtRepository.findByProvinceId(provinceId);
    }

    @Override
    public List<Ward> getWardsByDistrict(int districtId) {
        return wardRepository.findByDistrictId(districtId);
    }

    @Override
    public Ward getWardById(int wardId) {
        return wardRepository.findById(wardId).orElseThrow();
    }
}
