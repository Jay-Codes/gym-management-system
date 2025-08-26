package com.jerrycode.gym_services.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import java.time.LocalDate;

@Data
public class InvoiceRequest {
    private Long id;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_phone")
    private String userPhone;

    @Email
    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("member_name")
    private String memberName;

    @JsonProperty("member_id")
    private Long memberId;

    @JsonProperty("member_phone")
    private String memberPhone;

    @JsonProperty("amount_paid")
    private double amountPaid;

    private String status;

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("discount_percentage")
    private Double discountPercentage;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    private String memo;

    @Override
    public String toString() {
        return "InvoiceRequest{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", memberName='" + memberName + '\'' +
                ", memberId=" + memberId +
                ", memberPhone='" + memberPhone + '\'' +
                ", amountPaid=" + amountPaid +
                ", status='" + status + '\'' +
                ", packageName='" + packageName + '\'' +
                ", discountPercentage=" + discountPercentage +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", memo='" + memo + '\'' +
                '}';
    }

}
