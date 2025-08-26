package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.CompanyProfileManager;
import com.jerrycode.gym_services.request.CompanyProfileRequest;
import com.jerrycode.gym_services.response.CompanyProfileResponse;
import com.jerrycode.gym_services.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api/company")
public class CompanyProfileController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyProfileController.class);
    private final CompanyProfileManager companyProfileManager;

    public CompanyProfileController(CompanyProfileManager companyProfileManager) {
        this.companyProfileManager = companyProfileManager;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Response<CompanyProfileResponse>> updateOrCreateCompanyProfile(
            @ModelAttribute @Valid CompanyProfileRequest request,
            @RequestParam("logo") MultipartFile logo) {
        logger.info("Processing update or create company profile request {} " ,request);
        Response<CompanyProfileResponse> response = companyProfileManager.updateOrCreateCompanyProfile(request, logo);
        logger.info("Company Profile Update or Create Response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping()
    public ResponseEntity<Response<CompanyProfileResponse>> getCompanyProfile() {
        logger.info("Fetching company profile");
        Response<CompanyProfileResponse> response = companyProfileManager.getCompanyProfile();
        logger.info("Fetched Company Profile Response {}",response);
        return buildResponseEntity(response);
    }

    @DeleteMapping()
    public ResponseEntity<Response<CompanyProfileResponse>> deleteCompanyProfile() {
        logger.info("Deleting company profile");
        Response<CompanyProfileResponse> response = companyProfileManager.deleteCompanyProfile();
        logger.info("Delete Company Response {}",response);
        return buildResponseEntity(response);
    }

}