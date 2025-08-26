package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.CompanyProfileRepository;
import com.jerrycode.gym_services.data.vo.CompanyProfile;
import com.jerrycode.gym_services.utils.Language;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CompanyProfileDataInitializer {

    private final CompanyProfileRepository companyProfileRepository;

    public CompanyProfileDataInitializer(CompanyProfileRepository companyProfileRepository) {
        this.companyProfileRepository = companyProfileRepository;
    }

    @PostConstruct
    public void init() {
        // Check if data already exists
        if (companyProfileRepository.count() == 0) {
            CompanyProfile defaultCompany = CompanyProfile.builder()
                    .companyName("4J's Fitness Center")
                    .companyEmail("contact@4jsfitness.com")
                    .tin("123456789")
                    .description("Your ultimate fitness partner.")
                    .address("Temeke Street, Dar es Salaam, Tanzania")
                    .phone("0767413968")
                    .website("https://4jsfitness.com")
                    .founder("John Doe")
                    .manager("Jane Doe")
                    .accountName("4JS FITNESS CENTER")
                    .accountNumber("61057119")
                    .preferredLanguage(Language.SW)
                    .build();

            companyProfileRepository.save(defaultCompany);
            System.out.println("Default company profile created successfully");
        }
    }
}
