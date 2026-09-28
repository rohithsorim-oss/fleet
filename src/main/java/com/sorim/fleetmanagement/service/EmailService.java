package com.sorim.fleetmanagement.service;

import com.sorim.fleetmanagement.dto.request.EmailReportRequest;
import com.sorim.fleetmanagement.dto.response.ApiResponse;

public interface EmailService {
    ApiResponse<Void> sendVehicleReportEmail(EmailReportRequest request);
}
