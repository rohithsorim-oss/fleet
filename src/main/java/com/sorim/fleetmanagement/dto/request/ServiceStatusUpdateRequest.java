package com.sorim.fleetmanagement.dto.request;

import com.sorim.fleetmanagement.entity.ServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ServiceStatus status;

    private BigDecimal actualCost;
    private String technicianNotes;
}
