package com.jerrycode.gym_services.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AllCountsResponse {
    private Long members;
    private Long invoices;
    private Long paidMembers;
    private Long packages;
    private Long discounts;

    private TimeSeriesData daily;
    private TimeSeriesData weekly;
    private TimeSeriesData monthly;
}