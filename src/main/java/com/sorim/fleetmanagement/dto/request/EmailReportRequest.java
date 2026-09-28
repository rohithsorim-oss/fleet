package com.sorim.fleetmanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request to send vehicle report via email with attachment")
public class EmailReportRequest {
    
    @NotBlank(message = "Subject is required")
    @Schema(description = "Email subject", example = "Monthly Vehicle Report", required = true)
    private String subject;
    
    @NotBlank(message = "Body is required")
    @Schema(description = "Email body (supports HTML)", example = "<p>Please find attached the vehicle report.</p>", required = true)
    private String body;
    
    @NotEmpty(message = "Recipients are required")
    @Schema(description = "List of recipient email addresses", example = "[\"user1@example.com\", \"user2@example.com\"]", required = true)
    private List<@Email(message = "Invalid email format") String> to;
    
    @NotBlank(message = "Report format is required")
    @Schema(description = "Report format: 'pdf' or 'excel'", example = "excel", required = true)
    private String format;
    
    @Schema(description = "Comma-separated list of columns to include", example = "vin,make,model,year,status")
    private String columns;
    
    @Schema(description = "Search term to filter vehicles", example = "Toyota")
    private String search;
    
    @Schema(description = "Filter by vehicle status", example = "AVAILABLE")
    private String status;
    
    @Schema(description = "Filter by vehicle category ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "Minimum year filter", example = "2020")
    private Integer minYear;
    
    @Schema(description = "Maximum year filter", example = "2024")
    private Integer maxYear;
    
    @Schema(description = "Field to sort by", example = "year")
    private String sortBy;
    
    @Schema(description = "Sort direction (asc or desc)", example = "desc")
    private String sortDir;
}
