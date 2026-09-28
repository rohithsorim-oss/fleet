package com.sorim.fleetmanagement.service;

import org.springframework.http.ResponseEntity;

public interface ReportService {
    ResponseEntity<byte[]> generateVehicleReport(
            String format,
            java.util.List<String> columns,
            String search,
            String status,
            Long categoryId,
            Integer minYear,
            Integer maxYear,
            String sortBy,
            String sortDir
    );
}
