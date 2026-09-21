package com.sorim.fleetmanagement.dto.response;

import com.sorim.fleetmanagement.entity.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private String vin;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String color;
    private Integer mileage;
    private BigDecimal dailyRentalRate;
    private VehicleStatus status;
    private String imageUrl;
    private CategoryResponse category;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String code;
    }
}
