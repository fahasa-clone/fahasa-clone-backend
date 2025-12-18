package vn.clone.fahasa_backend.controller;

import java.util.List;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.clone.fahasa_backend.domain.District;
import vn.clone.fahasa_backend.domain.Province;
import vn.clone.fahasa_backend.domain.Ward;
import vn.clone.fahasa_backend.service.LocationService;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/provinces")
    public ResponseEntity<List<Province>> getAllProvinces() {
        List<Province> provinceList = locationService.getAllProvinces();
        return ResponseEntity.ok(provinceList);
    }

    @GetMapping("/districts/{province_id}")
    public ResponseEntity<List<District>> getDistrictsByProvince(@PathVariable("province_id") @Min(1) int provinceId) {
        List<District> districtList = locationService.getDistrictsByProvince(provinceId);
        return ResponseEntity.ok(districtList);
    }

    @GetMapping("/wards/{district_id}")
    public ResponseEntity<List<Ward>> getWardsByDistrict(@PathVariable("district_id") @Min(1) int districtId) {
        List<Ward> wardList = locationService.getWardsByDistrict(districtId);
        return ResponseEntity.ok(wardList);
    }
}
