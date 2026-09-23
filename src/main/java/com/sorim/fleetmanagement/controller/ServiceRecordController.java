package com.sorim.fleetmanagement.controller;

import com.sorim.fleetmanagement.dto.request.ServiceRecordCreateRequest;
import com.sorim.fleetmanagement.dto.request.ServiceStatusUpdateRequest;
import com.sorim.fleetmanagement.dto.response.ApiResponse;
import com.sorim.fleetmanagement.dto.response.PageResponse;
import com.sorim.fleetmanagement.dto.response.ServiceRecordResponse;
import com.sorim.fleetmanagement.entity.ServiceStatus;
import com.sorim.fleetmanagement.service.ServiceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Service Records", description = "Service record management endpoints")
public class ServiceRecordController {

    private final ServiceRecordService serviceRecordService;

    @GetMapping
    @Operation(
            summary = "Get all service records with pagination and filters",
            description = "Retrieves a paginated list of service records with optional filtering by status, vehicle ID, and user ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Service records retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<ServiceRecordResponse>>> getAllServiceRecords(
            @Parameter(description = "Filter by service status", schema = @Schema(type = "string", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"}))
            @RequestParam(required = false) ServiceStatus status,
            @Parameter(description = "Filter by vehicle ID", example = "1")
            @RequestParam(required = false) Long vehicleId,
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ServiceRecordResponse> response = serviceRecordService.getAllServiceRecords(
                status, vehicleId, userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Service records retrieved successfully", response));
    }

    @PostMapping
    @Operation(
            summary = "Create a new service record",
            description = "Creates a new service appointment for a vehicle. The service will be scheduled for the specified date."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Service appointment booked successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ServiceRecordResponse>> createServiceRecord(
            @Parameter(description = "Service record creation details", required = true)
            @Valid @RequestBody ServiceRecordCreateRequest request) {
        ServiceRecordResponse response = serviceRecordService.createServiceRecord(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service appointment booked successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update service record status (Admin only)",
            description = "Updates the status of a service record. Requires ADMIN role. Used to mark services as in progress or completed."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Service status updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Service record not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ServiceRecordResponse>> updateServiceStatus(
            @Parameter(description = "Service record ID", example = "1", required = true)
            @PathVariable @NotNull Long id,
            @Parameter(description = "Status update details", required = true)
            @Valid @RequestBody ServiceStatusUpdateRequest request) {
        ServiceRecordResponse response = serviceRecordService.updateServiceStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service status updated successfully", response));
    }
}
