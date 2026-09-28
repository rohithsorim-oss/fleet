package com.sorim.fleetmanagement.service.impl;

import com.sorim.fleetmanagement.dto.request.EmailReportRequest;
import com.sorim.fleetmanagement.dto.response.ApiResponse;
import com.sorim.fleetmanagement.service.EmailService;
import com.sorim.fleetmanagement.service.ReportService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ReportService reportService;

    @Override
    public ApiResponse<Void> sendVehicleReportEmail(EmailReportRequest request) {
        try {
            java.util.List<String> columnList = request.getColumns() != null && !request.getColumns().trim().isEmpty()
                    ? Arrays.asList(request.getColumns().split(","))
                    : null;

            var reportResponse = reportService.generateVehicleReport(
                    request.getFormat(),
                    columnList,
                    request.getSearch(),
                    request.getStatus(),
                    request.getCategoryId(),
                    request.getMinYear(),
                    request.getMaxYear(),
                    request.getSortBy(),
                    request.getSortDir()
            );

            byte[] reportBytes = reportResponse.getBody();
            String contentType = reportResponse.getHeaders().getContentType().toString();
            String filename = extractFilename(reportResponse.getHeaders().getContentDisposition(), request.getFormat());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(request.getTo().toArray(new String[0]));
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), true);

            helper.addAttachment(filename, new org.springframework.core.io.ByteArrayResource(reportBytes) {
                public String getContentType() {
                    return contentType;
                }
            });

            mailSender.send(message);
            log.info("Vehicle report email sent successfully to: {}", request.getTo());

            return ApiResponse.success("Email sent successfully", null);
        } catch (Exception e) {
            log.error("Failed to send vehicle report email", e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private String extractFilename(org.springframework.http.ContentDisposition contentDisposition, String format) {
        if (contentDisposition != null && contentDisposition.getFilename() != null) {
            return contentDisposition.getFilename();
        }
        return "vehicle_report." + ("excel".equalsIgnoreCase(format) ? "xlsx" : "pdf");
    }
}
