package com.jerrycode.gym_services.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class CompanyProfileRequest {
    private Long id;
    @NotBlank
    private String company_name;

    @Email
    private String company_email;

    private String tin;
    private String description;
    private String address;
    private String phone;
    private String website;
    private String founder;
    private String manager;
    private String account_name;
    private String account_number;

    @Override
    public String toString() {
        return "CompanyProfileRequest{" +
                "id=" + id +
                ", companyName='" + company_name + '\'' +
                ", companyEmail='" + company_email + '\'' +
                ", tin='" + tin + '\'' +
                ", description='" + description + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", founder='" + founder + '\'' +
                ", manager='" + manager + '\'' +
                ", accountName='" + account_name + '\'' +
                ", accountNumber='" + account_number + '\'' +
                '}';
    }
}
