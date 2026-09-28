package com.sorim.fleetmanagement.controller;

import com.google.common.base.Strings;
import com.sorim.fleetmanagement.dto.request.EmailReportRequest;
import com.sorim.fleetmanagement.dto.request.VehicleCreateRequest;
import com.sorim.fleetmanagement.dto.request.VehicleUpdateRequest;
import com.sorim.fleetmanagement.dto.response.ApiResponse;
import com.sorim.fleetmanagement.dto.response.PageResponse;
import com.sorim.fleetmanagement.dto.response.VehicleResponse;
import com.sorim.fleetmanagement.entity.VehicleStatus;
import com.sorim.fleetmanagement.service.EmailService;
import com.sorim.fleetmanagement.service.ReportService;
import com.sorim.fleetmanagement.service.VehicleService;

import java.util.Arrays;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management endpoints")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ReportService reportService;
    private final EmailService emailService;

    @GetMapping
    @Operation(
            summary = "Get all vehicles with pagination, search, and filters",
            description = "Retrieves a paginated list of vehicles with optional filtering by status, category, year range, and text search. Supports sorting by any field."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicles retrieved successfully",
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
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getAllVehicles(
            @Parameter(description = "Search term to filter vehicles by make, model, or license plate", example = "Toyota")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by vehicle status")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by vehicle category ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum year filter (inclusive)", example = "2020")
            @RequestParam(required = false) Integer minYear,
            @Parameter(description = "Maximum year filter (inclusive)", example = "2024")
            @RequestParam(required = false) Integer maxYear,
            @Parameter(description = "Field to sort by", example = "year")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        VehicleStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = VehicleStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status value, ignore and treat as null
            }
        }
        search = search == null ? "" : search;
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<VehicleResponse> response;
        if (Strings.isNullOrEmpty(search) && Strings.isNullOrEmpty(status)) {
            response = vehicleService.getAllVehicles(sortBy, sortDir, pageable);
        } else {
            response = vehicleService.getAllVehicles(
                    search, statusEnum, categoryId, minYear, maxYear, sortBy, sortDir, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get vehicle by ID",
            description = "Retrieves detailed information about a specific vehicle by its unique identifier."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(
            @Parameter(description = "Vehicle ID", example = "1", required = true)
            @PathVariable @NotNull Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new vehicle (Admin only)",
            description = "Creates a new vehicle entry in the system. Requires ADMIN role. All fields except color and image URL are mandatory."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Vehicle created successfully",
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
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Parameter(description = "Vehicle creation details", required = true)
            @Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update vehicle (Admin only)",
            description = "Updates an existing vehicle's information. Requires ADMIN role. VIN cannot be modified after creation."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation errors",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found",
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
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @Parameter(description = "Vehicle ID", example = "1", required = true)
            @PathVariable @NotNull Long id,
            @Parameter(description = "Vehicle update details", required = true)
            @Valid @RequestBody VehicleUpdateRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete vehicle (Admin only)",
            description = "Permanently deletes a vehicle from the system. This action cannot be undone. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Vehicle deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found",
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
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @Parameter(description = "Vehicle ID", example = "1", required = true)
            @PathVariable @NotNull Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully", null));
    }

    @GetMapping("/report")
    @Operation(
            summary = "Download vehicle report as PDF or Excel",
            description = "Generates and downloads a vehicle report in either PDF or Excel format. Supports filtering by status, category, year range, and text search. Allows column selection to customize the report content. The response is a downloadable file."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Report generated successfully - returns downloadable file (PDF or Excel)"
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
    public ResponseEntity<byte[]> downloadVehicleReport(
            @Parameter(description = "Report format: 'pdf' or 'excel'", example = "excel", required = true)
            @RequestParam @NotBlank String format,
            @Parameter(description = "Comma-separated list of columns to include. Available columns: id, vin, make, model, year, licensePlate, color, mileage, dailyRentalRate, status, category, createdAt. If not specified, all columns are included.", example = "vin,make,model,year,status")
            @RequestParam(required = false) String columns,
            @Parameter(description = "Search term to filter vehicles by make, model, or license plate", example = "Toyota")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by vehicle status", example = "AVAILABLE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by vehicle category ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum year filter (inclusive)", example = "2020")
            @RequestParam(required = false) Integer minYear,
            @Parameter(description = "Maximum year filter (inclusive)", example = "2024")
            @RequestParam(required = false) Integer maxYear,
            @Parameter(description = "Field to sort by", example = "year")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        java.util.List<String> columnList = columns != null && !columns.trim().isEmpty() 
                ? Arrays.asList(columns.split(",")) 
                : null;

        return reportService.generateVehicleReport(
                format,
                columnList,
                search,
                status,
                categoryId,
                minYear,
                maxYear,
                sortBy,
                sortDir
        );
    }

    @PostMapping("/report/email")
    @Operation(
            summary = "Send vehicle report via email with attachment",
            description = "Generates a vehicle report (PDF or Excel) and sends it as an email attachment to specified recipients. Supports all the same filtering options as the report download endpoint. The email body supports HTML content."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email sent successfully",
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
            )
    })
    public ResponseEntity<ApiResponse<Void>> sendVehicleReportEmail(
            @Parameter(description = "Email request details including recipients, subject, body, and report parameters", required = true)
            @Valid @RequestBody EmailReportRequest request) {
        
        ApiResponse<Void> response = emailService.sendVehicleReportEmail(request);
        return ResponseEntity.ok(response);
    }
}
