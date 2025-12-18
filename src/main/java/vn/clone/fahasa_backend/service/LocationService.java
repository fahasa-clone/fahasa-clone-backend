package vn.clone.fahasa_backend.service;

import java.util.List;

import vn.clone.fahasa_backend.domain.District;
import vn.clone.fahasa_backend.domain.Province;
import vn.clone.fahasa_backend.domain.Ward;

public interface LocationService {

    List<Province> getAllProvinces();

    List<District> getDistrictsByProvince(int provinceId);

    List<Ward> getWardsByDistrict(int districtId);

    Ward getWardById(int wardId);
}
