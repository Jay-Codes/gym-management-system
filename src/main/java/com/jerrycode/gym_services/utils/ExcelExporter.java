package com.jerrycode.gym_services.utils;

import com.jerrycode.gym_services.data.vo.Invoices;
import com.jerrycode.gym_services.data.vo.Member;
import com.jerrycode.gym_services.response.MonthlyReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ExcelExporter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExporter.class);
    private static final String MONTHLY_REPORT_SHEET = "Monthly Report";
    private static final String MEMBERS_INVOICES_SHEET = "Members Invoices";
    private static final int MAX_ROWS_FOR_AUTOSIZE = 1000; // Limit for performance

    // Common method to create header row with styling
    private void createHeaderRow(Sheet sheet, String[] headers) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("Headers cannot be null or empty");
        }

        logger.debug("Creating header row with {} columns", headers.length);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    public void exportMonthlyReport(MonthlyReportResponse reportData, ByteArrayOutputStream outputStream) throws IOException {
        logger.info("Starting export of monthly report for year: {}", reportData != null ? reportData.getYear() : "unknown");

        if (reportData == null || reportData.getMembers() == null || reportData.getPaid() == null || reportData.getInvoices() == null) {
            logger.warn("Invalid monthly report data");
            throw new IllegalArgumentException("Monthly report data cannot be null");
        }

        if (reportData.getMembers().isEmpty() && reportData.getPaid().isEmpty() && reportData.getInvoices().isEmpty()) {
            logger.warn("No monthly data available to export");
            throw new IllegalArgumentException("No monthly data available to export");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(MONTHLY_REPORT_SHEET);

            // Headers
            String[] headers = {"Month", "New Members", "Paid Members", "Total Amount"};
            createHeaderRow(sheet, headers);

            // Styles
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00")); // Adjust for TZS if needed

            int rowNum = 1;

            // Assuming all lists have 12 elements in the same order (i.e., months Jan–Dec)
            for (int i = 0; i < reportData.getMembers().size(); i++) {
                String month = reportData.getMembers().get(i).getMonth();
                long newMembers = reportData.getMembers().get(i).getTotal();
                long paidMembers = reportData.getPaid().get(i).getTotal();
                String totalAmountStr = reportData.getInvoices().get(i).getTotal();

                double totalAmount = 0.0;
                try {
                    totalAmount = Double.parseDouble(totalAmountStr);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid total amount for month {}: {}", month, totalAmountStr);
                }

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(month);

                Cell newMembersCell = row.createCell(1);
                newMembersCell.setCellValue(newMembers);
                newMembersCell.setCellStyle(numberStyle);

                Cell paidMembersCell = row.createCell(2);
                paidMembersCell.setCellValue(paidMembers);
                paidMembersCell.setCellStyle(numberStyle);

                Cell totalAmountCell = row.createCell(3);
                totalAmountCell.setCellValue(totalAmount);
                totalAmountCell.setCellStyle(currencyStyle);
            }

            // Auto-size columns if small
            if (rowNum <= MAX_ROWS_FOR_AUTOSIZE) {
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(outputStream);
            logger.info("Monthly report exported successfully with {} rows", rowNum - 1);
        } catch (IOException e) {
            logger.error("Failed to export monthly report to Excel: {}", e.getMessage());
            throw new IOException("Error writing Excel file for monthly report: " + e.getMessage(), e);
        }
    }


    public void exportMembersInvoices(List<Member> members, Map<Member, List<Invoices>> memberInvoicesMap, ByteArrayOutputStream outputStream) throws IOException {
        logger.info("Starting export of members invoices");
        if (members == null || memberInvoicesMap == null) {
            logger.warn("Invalid members or invoices data");
            throw new IllegalArgumentException("Members or invoices data cannot be null");
        }
        if (members.isEmpty()) {
            logger.warn("No members available to export");
            throw new IllegalArgumentException("No members available to export");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(MEMBERS_INVOICES_SHEET);

            // Headers
            String[] headers = {"Member ID", "Member Name", "Phone", "Email", "Invoice Count", "Total Paid"};
            createHeaderRow(sheet, headers);

            // Data rows
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00")); // Adjust for TZS if needed

            int rowNum = 1;
            for (Member member : members) {
                if (member.getId() == null || member.getName() == null || member.getPhoneNumber() == null) {
                    logger.warn("Skipping invalid member with missing required fields: id={}, name={}, phone={}",
                            member.getId(), member.getName(), member.getPhoneNumber());
                    continue;
                }

                List<Invoices> invoices = memberInvoicesMap.getOrDefault(member, Collections.emptyList());
                int invoiceCount = invoices.size();
                double totalPaid = invoices.stream()
                        .mapToDouble(Invoices::getAmountPaid)
                        .sum();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(member.getId());
                row.createCell(1).setCellValue(member.getName());
                row.createCell(2).setCellValue(member.getPhoneNumber());
                row.createCell(3).setCellValue(member.getEmail() != null ? member.getEmail() : "N/A");

                Cell invoiceCountCell = row.createCell(4);
                invoiceCountCell.setCellValue(invoiceCount);
                invoiceCountCell.setCellStyle(numberStyle);

                Cell totalPaidCell = row.createCell(5);
                totalPaidCell.setCellValue(totalPaid);
                totalPaidCell.setCellStyle(currencyStyle);
            }

            // Auto-size columns only for small datasets
            if (rowNum <= MAX_ROWS_FOR_AUTOSIZE) {
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(outputStream);
            logger.info("Members invoices exported successfully with {} rows", rowNum - 1);
        } catch (IOException e) {
            logger.error("Failed to export members invoices to Excel: {}", e.getMessage());
            throw new IOException("Error writing Excel file for members invoices: " + e.getMessage(), e);
        }
    }
}