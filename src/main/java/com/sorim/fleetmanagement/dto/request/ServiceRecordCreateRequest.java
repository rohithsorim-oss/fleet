package com.sorim.fleetmanagement.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ServiceRecordCreateRequest {
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotBlank(message = "Service type is required")
    @Size(max = 100, message = "Service type must not exceed 100 characters")
    private String serviceType;

    private String description;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @DecimalMin(value = "0.00", message = "Estimated cost cannot be negative")
    private BigDecimal estimatedCost;
}
