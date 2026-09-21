package com.sorim.fleetmanagement.service;

import com.sorim.fleetmanagement.dto.request.ServiceRecordCreateRequest;
import com.sorim.fleetmanagement.dto.request.ServiceStatusUpdateRequest;
import com.sorim.fleetmanagement.dto.response.PageResponse;
import com.sorim.fleetmanagement.dto.response.ServiceRecordResponse;
import com.sorim.fleetmanagement.entity.ServiceStatus;
import org.springframework.data.domain.Pageable;

public interface ServiceRecordService {
    PageResponse<ServiceRecordResponse> getAllServiceRecords(ServiceStatus status, Long vehicleId, Long userId, Pageable pageable);
    ServiceRecordResponse getServiceRecordById(Long id);
    ServiceRecordResponse createServiceRecord(ServiceRecordCreateRequest request);
    ServiceRecordResponse updateServiceStatus(Long id, ServiceStatusUpdateRequest request);
}
