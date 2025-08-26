package com.jerrycode.gym_services.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonthlyReportResponse {
    private int year;
    private List<MonthlyCount> members = new ArrayList<>();
    private List<MonthlyCount> paid = new ArrayList<>();
    private List<MonthlyAmount> invoices = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class MonthlyCount {
        private String month;
        private long total;
    }

    @Data
    @AllArgsConstructor
    public static class MonthlyAmount {
        private String month;
        private String total;
    }

    public void addMemberData(String month, long total) {
        members.add(new MonthlyCount(month, total));
    }

    public void addPaidData(String month, long total) {
        paid.add(new MonthlyCount(month, total));
    }

    public void addInvoiceData(String month, double total) {
        invoices.add(new MonthlyAmount(month, String.format("%.2f", total)));
    }
}
