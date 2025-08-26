package com.jerrycode.gym_services.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse {
    private Long id;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_phone")
    private String userPhone;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("member_name")
    private String memberName;

    @JsonProperty("member_phone")
    private String memberPhone;

    @JsonProperty("amount_paid")
    private String amountPaid;

    private String status;

    private Integer paid;

    private String memo;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("discount_percentage")
    private String discountPercentage;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("invoice_file")
    private String invoiceFile;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
