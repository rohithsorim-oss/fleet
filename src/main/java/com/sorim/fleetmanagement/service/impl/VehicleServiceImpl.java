package com.sorim.fleetmanagement.service.impl;

import com.sorim.fleetmanagement.dto.request.VehicleCreateRequest;
import com.sorim.fleetmanagement.dto.request.VehicleUpdateRequest;
import com.sorim.fleetmanagement.dto.response.PageResponse;
import com.sorim.fleetmanagement.dto.response.VehicleResponse;
import com.sorim.fleetmanagement.entity.User;
import com.sorim.fleetmanagement.entity.Vehicle;
import com.sorim.fleetmanagement.entity.VehicleCategory;
import com.sorim.fleetmanagement.entity.VehicleStatus;
import com.sorim.fleetmanagement.exception.BadRequestException;
import com.sorim.fleetmanagement.exception.ResourceNotFoundException;
import com.sorim.fleetmanagement.repository.VehicleCategoryRepository;
import com.sorim.fleetmanagement.repository.VehicleRepository;
import com.sorim.fleetmanagement.security.UserPrincipal;
import com.sorim.fleetmanagement.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository categoryRepository;

    @Override
    public PageResponse<VehicleResponse> getAllVehicles(String search, VehicleStatus status, Long categoryId,
                                                          Integer minYear, Integer maxYear, String sortBy,
                                                          String sortDir, Pageable pageable) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Vehicle> vehicles = vehicleRepository.searchVehicles(search, status, categoryId, minYear, maxYear, sortedPageable);
        return PageResponse.of(vehicles.map(this::mapToResponse));
    }

    @Override
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
        return mapToResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        if (vehicleRepository.existsByVin(request.getVin())) {
            throw new BadRequestException("Vehicle with VIN " + request.getVin() + " already exists");
        }
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BadRequestException("Vehicle with license plate " + request.getLicensePlate() + " already exists");
        }

        VehicleCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("VehicleCategory", request.getCategoryId()));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = new User();
        currentUser.setId(userPrincipal.getId());

        Vehicle vehicle = new Vehicle();
        vehicle.setVin(request.getVin());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setColor(request.getColor());
        vehicle.setMileage(request.getMileage());
        vehicle.setDailyRentalRate(request.getDailyRentalRate());
        vehicle.setStatus(request.getStatus() != null ? request.getStatus() : VehicleStatus.AVAILABLE);
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setCategory(category);
        vehicle.setCreatedBy(currentUser);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(savedVehicle);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));

        if (!vehicle.getLicensePlate().equals(request.getLicensePlate()) &&
            vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new BadRequestException("Vehicle with license plate " + request.getLicensePlate() + " already exists");
        }

        VehicleCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("VehicleCategory", request.getCategoryId()));

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setColor(request.getColor());
        vehicle.setMileage(request.getMileage());
        vehicle.setDailyRentalRate(request.getDailyRentalRate());
        vehicle.setStatus(request.getStatus());
        vehicle.setImageUrl(request.getImageUrl());
        vehicle.setCategory(category);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return mapToResponse(updatedVehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        VehicleResponse.CategoryResponse categoryResponse = new VehicleResponse.CategoryResponse(
                vehicle.getCategory().getId(),
                vehicle.getCategory().getName(),
                vehicle.getCategory().getCode()
        );

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getLicensePlate(),
                vehicle.getColor(),
                vehicle.getMileage(),
                vehicle.getDailyRentalRate(),
                vehicle.getStatus(),
                vehicle.getImageUrl(),
                categoryResponse,
                vehicle.getCreatedAt()
        );
    }
}
