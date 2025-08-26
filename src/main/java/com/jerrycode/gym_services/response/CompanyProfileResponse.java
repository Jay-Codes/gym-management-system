package com.jerrycode.gym_services.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyProfileResponse {
    private Long id;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("company_email")
    private String companyEmail;
    private String tin;
    private String description;
    private String address;
    private String phone;
    private String website;
    private String founder;
    private String manager;
    @JsonProperty("account_name")
    private String accountName;
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("logo")
    private String logoUrl;
}
