package com.sorim.fleetmanagement.dto.response;

import com.sorim.fleetmanagement.entity.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecordResponse {
    private Long id;
    private VehicleSummary vehicle;
    private UserSummary user;
    private String serviceType;
    private String description;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private ServiceStatus status;
    private String technicianNotes;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleSummary {
        private Long id;
        private String make;
        private String model;
        private String licensePlate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String email;
    }
}
