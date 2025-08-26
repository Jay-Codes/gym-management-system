package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.InvoiceRepository;
import com.jerrycode.gym_services.data.dao.MemberRepository;
import com.jerrycode.gym_services.data.vo.Invoices;
import com.jerrycode.gym_services.data.vo.Member;
import com.jerrycode.gym_services.response.MonthlyReportResponse;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.utils.ExcelExporter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService implements ReportManager {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final InvoiceRepository invoiceRepository;
    private final MemberRepository memberRepository;
    private final ExcelExporter excelExporter;

    @Override
    public Response<MonthlyReportResponse> getMonthlyReports(Integer year) {
        Response<MonthlyReportResponse> response = new Response<>();
        try {
            logger.info("Processing monthly reports request for year: {}", year != null ? year : "current year");

            if (year != null && year < 2000) {
                return Response.<MonthlyReportResponse>builder()
                        .success(false)
                        .message("Year must be 2000 or later")
                        .build();
            }

            int effectiveYear = year != null ? year : Year.now().getValue();

            MonthlyReportResponse report = new MonthlyReportResponse();
            report.setYear(effectiveYear);

            Arrays.stream(Month.values()).forEach(month -> {
                LocalDate startDate = LocalDate.of(effectiveYear, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

                long newMembers = memberRepository.countByCreatedAtBetween(
                        startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

                long paidMembers = invoiceRepository.countDistinctMemberByPaidTrueAndCreatedAtBetween(
                        startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

                Double totalAmount = invoiceRepository.sumAmountPaidByCreatedAtBetween(
                        startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

                String formattedMonth = month.name().charAt(0) + month.name().substring(1).toLowerCase();

                report.addMemberData(formattedMonth, newMembers);
                report.addPaidData(formattedMonth, paidMembers);
                report.addInvoiceData(formattedMonth, totalAmount != null ? totalAmount : 0.0);
            });

            response.setSuccess(true);
            response.setMessage("Report retrieved successfully");
            response.setData(report);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving monthly reports: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Returning processed monthly reports response {}",response);
        return response;
    }


    @Override
    public Response<Resource> downloadReport(Integer year) {
        Response<Resource> response = new Response<>();
        try {
            logger.info("Processing download report request for year: {}", year != null ? year : "current year");

            // Validate year
            if (year != null && year < 2000) {
                logger.warn("Invalid year: {}", year);
                return Response.<Resource>builder()
                        .success(false)
                        .message("Year must be 2000 or later")
                        .build();
            }

            // Default to current year if null
            int effectiveYear = year != null ? year : Year.now().getValue();

            // Generate report data
            Response<MonthlyReportResponse> reportResponse = getMonthlyReports(effectiveYear);
            if (!reportResponse.isSuccess()) {
                logger.warn("Failed to generate report data: {}", reportResponse.getMessage());
                return Response.<Resource>builder()
                        .success(false)
                        .message(reportResponse.getMessage())
                        .build();
            }

            // Generate Excel file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelExporter.exportMonthlyReport(reportResponse.getData(), outputStream);
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            response.setSuccess(true);
            response.setMessage("Report downloaded successfully");
            response.setData(resource);
            logger.info("Report downloaded successfully for year: {}", effectiveYear);
        } catch (Exception e) {
            logger.error("Unexpected error downloading report: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Downloading report response {}",response);
        return response;
    }

    @Override
    public Response<Resource> exportMembersInvoices() {
        Response<Resource> response = new Response<>();
        try {
            logger.info("Processing export members invoices request");

            // Fetch members and invoices
            List<Member> members = memberRepository.findAll();
            if (members.isEmpty()) {
                logger.warn("No members found for export");
                return Response.<Resource>builder()
                        .success(false)
                        .message("No members found for export")
                        .build();
            }

            Map<Member, List<Invoices>> memberInvoicesMap = members.stream()
                    .collect(Collectors.toMap(
                            member -> member,
                            member -> invoiceRepository.findByMemberId(member.getId())
                    ));

            // Generate Excel file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            excelExporter.exportMembersInvoices(members, memberInvoicesMap, outputStream);
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            response.setSuccess(true);
            response.setMessage("Members invoices exported successfully");
            response.setData(resource);
            logger.info("Members invoices exported successfully");
        } catch (Exception e) {
            logger.error("Unexpected error exporting members invoices: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Exporting members invoices response back {}",response);
        return response;
    }
}