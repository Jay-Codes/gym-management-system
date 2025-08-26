package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.response.MonthlyReportResponse;
import com.jerrycode.gym_services.response.Response;
import org.springframework.core.io.Resource;

public interface ReportManager {
    Response<MonthlyReportResponse> getMonthlyReports(Integer year);
    Response<Resource> downloadReport(Integer year);
    Response<Resource> exportMembersInvoices();
}