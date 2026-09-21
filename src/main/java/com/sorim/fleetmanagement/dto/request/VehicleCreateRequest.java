package com.sorim.fleetmanagement.dto.request;

import com.sorim.fleetmanagement.entity.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleCreateRequest {
    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    @NotBlank(message = "Make is required")
    @Size(max = 50, message = "Make must not exceed 50 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1990, message = "Year must be 1990 or later")
    private Integer year;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @Size(max = 30, message = "Color must not exceed 30 characters")
    private String color;

    @NotNull(message = "Mileage is required")
    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileage;

    @NotNull(message = "Daily rental rate is required")
    @DecimalMin(value = "0.00", message = "Daily rental rate cannot be negative")
    private BigDecimal dailyRentalRate;

    private VehicleStatus status;

    private String imageUrl;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
