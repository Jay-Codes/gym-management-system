package com.jerrycode.gym_services.data.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfileDTO {
    private Long id;
    private String logo;
    private String companyName;
    private String companyEmail;
    private String tin;
    private String description;
    private String address;
    private String phone;
    private String website;
    private String founder;
    private String manager;
    private String accountName;
    private String accountNumber;
    private String createdAt;
    private String updatedAt;
}
