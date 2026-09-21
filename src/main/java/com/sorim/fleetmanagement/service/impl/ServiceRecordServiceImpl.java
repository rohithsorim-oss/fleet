package com.sorim.fleetmanagement.service.impl;

import com.sorim.fleetmanagement.dto.request.ServiceRecordCreateRequest;
import com.sorim.fleetmanagement.dto.request.ServiceStatusUpdateRequest;
import com.sorim.fleetmanagement.dto.response.PageResponse;
import com.sorim.fleetmanagement.dto.response.ServiceRecordResponse;
import com.sorim.fleetmanagement.entity.Role;
import com.sorim.fleetmanagement.entity.ServiceRecord;
import com.sorim.fleetmanagement.entity.ServiceStatus;
import com.sorim.fleetmanagement.entity.User;
import com.sorim.fleetmanagement.entity.Vehicle;
import com.sorim.fleetmanagement.exception.BadRequestException;
import com.sorim.fleetmanagement.exception.ResourceNotFoundException;
import com.sorim.fleetmanagement.repository.ServiceRecordRepository;
import com.sorim.fleetmanagement.repository.VehicleRepository;
import com.sorim.fleetmanagement.security.UserPrincipal;
import com.sorim.fleetmanagement.service.ServiceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public PageResponse<ServiceRecordResponse> getAllServiceRecords(ServiceStatus status, Long vehicleId, Long userId, Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Long effectiveUserId = userId;
        if (userId == null && userPrincipal.getRole() == Role.ROLE_USER) {
            effectiveUserId = userPrincipal.getId();
        }

        Page<ServiceRecord> serviceRecords = serviceRecordRepository.searchServiceRecords(status, vehicleId, effectiveUserId, pageable);
        return PageResponse.of(serviceRecords.map(this::mapToResponse));
    }

    @Override
    public ServiceRecordResponse getServiceRecordById(Long id) {
        ServiceRecord serviceRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRecord", id));
        return mapToResponse(serviceRecord);
    }

    @Override
    @Transactional
    public ServiceRecordResponse createServiceRecord(ServiceRecordCreateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.getVehicleId()));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = new User();
        currentUser.setId(userPrincipal.getId());

        ServiceRecord serviceRecord = new ServiceRecord();
        serviceRecord.setVehicle(vehicle);
        serviceRecord.setUser(currentUser);
        serviceRecord.setServiceType(request.getServiceType());
        serviceRecord.setDescription(request.getDescription());
        serviceRecord.setScheduledDate(request.getScheduledDate());
        serviceRecord.setEstimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : BigDecimal.ZERO);
        serviceRecord.setStatus(ServiceStatus.SCHEDULED);

        ServiceRecord savedRecord = serviceRecordRepository.save(serviceRecord);
        return mapToResponse(savedRecord);
    }

    @Override
    @Transactional
    public ServiceRecordResponse updateServiceStatus(Long id, ServiceStatusUpdateRequest request) {
        ServiceRecord serviceRecord = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRecord", id));

        ServiceStatus newStatus = request.getStatus();
        ServiceStatus currentStatus = serviceRecord.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        serviceRecord.setStatus(newStatus);

        if (newStatus == ServiceStatus.COMPLETED) {
            serviceRecord.setCompletedDate(LocalDate.now());
            if (request.getActualCost() != null) {
                serviceRecord.setActualCost(request.getActualCost());
            }
        }

        if (request.getTechnicianNotes() != null) {
            serviceRecord.setTechnicianNotes(request.getTechnicianNotes());
        }

        ServiceRecord updatedRecord = serviceRecordRepository.save(serviceRecord);
        return mapToResponse(updatedRecord);
    }

    private boolean isValidStatusTransition(ServiceStatus current, ServiceStatus newStatus) {
        return switch (current) {
            case SCHEDULED -> newStatus == ServiceStatus.IN_PROGRESS || newStatus == ServiceStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == ServiceStatus.COMPLETED || newStatus == ServiceStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private ServiceRecordResponse mapToResponse(ServiceRecord record) {
        ServiceRecordResponse.VehicleSummary vehicleSummary = new ServiceRecordResponse.VehicleSummary(
                record.getVehicle().getId(),
                record.getVehicle().getMake(),
                record.getVehicle().getModel(),
                record.getVehicle().getLicensePlate()
        );

        ServiceRecordResponse.UserSummary userSummary = new ServiceRecordResponse.UserSummary(
                record.getUser().getId(),
                record.getUser().getFullName(),
                record.getUser().getEmail()
        );

        return new ServiceRecordResponse(
                record.getId(),
                vehicleSummary,
                userSummary,
                record.getServiceType(),
                record.getDescription(),
                record.getScheduledDate(),
                record.getCompletedDate(),
                record.getEstimatedCost(),
                record.getActualCost(),
                record.getStatus(),
                record.getTechnicianNotes(),
                record.getCreatedAt()
        );
    }
}
