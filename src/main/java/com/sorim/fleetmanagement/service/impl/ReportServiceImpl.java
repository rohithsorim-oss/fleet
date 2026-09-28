package com.sorim.fleetmanagement.service.impl;

import com.sorim.fleetmanagement.entity.Vehicle;
import com.sorim.fleetmanagement.entity.VehicleStatus;
import com.sorim.fleetmanagement.repository.VehicleRepository;
import com.sorim.fleetmanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final VehicleRepository vehicleRepository;

    private static final java.util.List<String> AVAILABLE_COLUMNS = Arrays.asList(
            "id", "vin", "make", "model", "year", "licensePlate", "color",
            "mileage", "dailyRentalRate", "status", "category", "createdAt"
    );

    private static final Map<String, String> COLUMN_HEADERS = new HashMap<>();
    static {
        COLUMN_HEADERS.put("id", "ID");
        COLUMN_HEADERS.put("vin", "VIN");
        COLUMN_HEADERS.put("make", "Make");
        COLUMN_HEADERS.put("model", "Model");
        COLUMN_HEADERS.put("year", "Year");
        COLUMN_HEADERS.put("licensePlate", "License Plate");
        COLUMN_HEADERS.put("color", "Color");
        COLUMN_HEADERS.put("mileage", "Mileage");
        COLUMN_HEADERS.put("dailyRentalRate", "Daily Rate");
        COLUMN_HEADERS.put("status", "Status");
        COLUMN_HEADERS.put("category", "Category");
        COLUMN_HEADERS.put("createdAt", "Created At");
    }

    @Override
    public ResponseEntity<byte[]> generateVehicleReport(
            String format,
            java.util.List<String> columns,
            String search,
            String status,
            Long categoryId,
            Integer minYear,
            Integer maxYear,
            String sortBy,
            String sortDir) {

        java.util.List<String> selectedColumns = validateAndFilterColumns(columns);
        
        VehicleStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = VehicleStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status value, ignore and treat as null
            }
        }

        String effectiveSearch = (search == null || search.trim().isEmpty()) ? null : search;
        String effectiveSortBy = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "id";
        String effectiveSortDir = (sortDir != null && !sortDir.trim().isEmpty()) ? sortDir : "desc";
        Sort sort = Sort.by(effectiveSortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, effectiveSortBy);
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);

        Page<Vehicle> vehicles = vehicleRepository.searchVehicles(
                effectiveSearch, statusEnum, categoryId, minYear, maxYear, pageable
        );

        byte[] reportBytes;
        String filename;
        String contentType;

        if ("excel".equalsIgnoreCase(format)) {
            reportBytes = generateExcelReport(vehicles.getContent(), selectedColumns);
            filename = "vehicles_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            reportBytes = generatePdfReport(vehicles.getContent(), selectedColumns);
            filename = "vehicles_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            contentType = "application/pdf";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(reportBytes);
    }

    private java.util.List<String> validateAndFilterColumns(java.util.List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return AVAILABLE_COLUMNS;
        }
        java.util.List<String> filtered = new ArrayList<>();
        for (String col : columns) {
            if (AVAILABLE_COLUMNS.contains(col)) {
                filtered.add(col);
            }
        }
        return filtered.isEmpty() ? AVAILABLE_COLUMNS : filtered;
    }

    private byte[] generateExcelReport(java.util.List<Vehicle> vehicles, java.util.List<String> columns) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Vehicles");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(COLUMN_HEADERS.get(columns.get(i)));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (Vehicle vehicle : vehicles) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < columns.size(); i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                    String column = columns.get(i);
                    setCellValue(cell, vehicle, column, formatter);
                }
            }

            // Auto-size columns
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void setCellValue(org.apache.poi.ss.usermodel.Cell cell, Vehicle vehicle, String column, DateTimeFormatter formatter) {
        switch (column) {
            case "id" -> cell.setCellValue(vehicle.getId());
            case "vin" -> cell.setCellValue(vehicle.getVin());
            case "make" -> cell.setCellValue(vehicle.getMake());
            case "model" -> cell.setCellValue(vehicle.getModel());
            case "year" -> cell.setCellValue(vehicle.getYear());
            case "licensePlate" -> cell.setCellValue(vehicle.getLicensePlate());
            case "color" -> cell.setCellValue(vehicle.getColor() != null ? vehicle.getColor() : "");
            case "mileage" -> cell.setCellValue(vehicle.getMileage());
            case "dailyRentalRate" -> cell.setCellValue(vehicle.getDailyRentalRate().doubleValue());
            case "status" -> cell.setCellValue(vehicle.getStatus().toString());
            case "category" -> cell.setCellValue(vehicle.getCategory() != null ? vehicle.getCategory().getName() : "");
            case "createdAt" -> cell.setCellValue(vehicle.getCreatedAt() != null ?
                    vehicle.getCreatedAt().format(formatter) : "");
        }
    }

    private byte[] generatePdfReport(java.util.List<Vehicle> vehicles, java.util.List<String> columns) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            org.openpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            org.openpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            org.openpdf.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph title = new Paragraph("Vehicle Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Paragraph generatedAt = new Paragraph("Generated: " + LocalDateTime.now().format(formatter), dataFont);
            generatedAt.setAlignment(Element.ALIGN_CENTER);
            document.add(generatedAt);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(columns.size());
            table.setWidthPercentage(100);

            // Header row
            for (String column : columns) {
                PdfPCell cell = new PdfPCell(new Phrase(COLUMN_HEADERS.get(column), headerFont));
                cell.setBackgroundColor(new java.awt.Color(200, 200, 200));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Data rows
            for (Vehicle vehicle : vehicles) {
                for (String column : columns) {
                    PdfPCell cell = new PdfPCell(new Phrase(getCellValue(vehicle, column, formatter), dataFont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private String getCellValue(Vehicle vehicle, String column, DateTimeFormatter formatter) {
        return switch (column) {
            case "id" -> String.valueOf(vehicle.getId());
            case "vin" -> vehicle.getVin();
            case "make" -> vehicle.getMake();
            case "model" -> vehicle.getModel();
            case "year" -> String.valueOf(vehicle.getYear());
            case "licensePlate" -> vehicle.getLicensePlate();
            case "color" -> vehicle.getColor() != null ? vehicle.getColor() : "";
            case "mileage" -> String.valueOf(vehicle.getMileage());
            case "dailyRentalRate" -> vehicle.getDailyRentalRate().toString();
            case "status" -> vehicle.getStatus().toString();
            case "category" -> vehicle.getCategory() != null ? vehicle.getCategory().getName() : "";
            case "createdAt" -> vehicle.getCreatedAt() != null ?
                    vehicle.getCreatedAt().format(formatter) : "";
            default -> "";
        };
    }
}
