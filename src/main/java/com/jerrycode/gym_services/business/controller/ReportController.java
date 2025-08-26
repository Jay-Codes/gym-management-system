package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.ReportManager;
import com.jerrycode.gym_services.response.MonthlyReportResponse;
import com.jerrycode.gym_services.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportManager reportManager;

    public ReportController(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    @GetMapping("/monthly-reports")
    public ResponseEntity<Response<MonthlyReportResponse>> getMonthlyReports(
            @RequestParam(required = false) @Positive Integer year) {
        logger.info("Fetching monthly reports for year: {}", year != null ? year : "all years");
        Response<MonthlyReportResponse> response = reportManager.getMonthlyReports(year);
        logger.info("Fetched monthly reports response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/download-report")
    public ResponseEntity<Response<Resource>> downloadReport(
            @RequestParam(required = false) @Positive Integer year) {
        logger.info("Downloading report for year: {}", year != null ? year : "all years");
        Response<Resource> response = reportManager.downloadReport(year);
        logger.info("Downloading report for a year response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/export-members-invoices")
    public ResponseEntity<Response<Resource>> exportMembersInvoices() {
        logger.info("Exporting members invoices request");
        Response<Resource> response = reportManager.exportMembersInvoices();
        logger.info("Exporting members invoices response {}",response);
        return buildResponseEntity(response);
    }

}