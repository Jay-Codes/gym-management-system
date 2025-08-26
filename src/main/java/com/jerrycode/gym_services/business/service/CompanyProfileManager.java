package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.request.CompanyProfileRequest;
import com.jerrycode.gym_services.response.CompanyProfileResponse;
import com.jerrycode.gym_services.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyProfileManager {
    Response<CompanyProfileResponse> updateOrCreateCompanyProfile(CompanyProfileRequest request, MultipartFile logo);
    Response<CompanyProfileResponse> getCompanyProfile();
    Response<CompanyProfileResponse> deleteCompanyProfile();
}
