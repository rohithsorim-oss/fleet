package com.sorim.fleetmanagement.service;

import com.sorim.fleetmanagement.dto.request.VehicleCreateRequest;
import com.sorim.fleetmanagement.dto.request.VehicleUpdateRequest;
import com.sorim.fleetmanagement.dto.response.PageResponse;
import com.sorim.fleetmanagement.dto.response.VehicleResponse;
import com.sorim.fleetmanagement.entity.VehicleStatus;
import org.springframework.data.domain.Pageable;

public interface VehicleService {
    PageResponse<VehicleResponse> getAllVehicles(String search, VehicleStatus status, Long categoryId,
                                                  Integer minYear, Integer maxYear, String sortBy, String sortDir,
                                                  Pageable pageable);
    VehicleResponse getVehicleById(Long id);
    VehicleResponse createVehicle(VehicleCreateRequest request);
    VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request);
    void deleteVehicle(Long id);
}
