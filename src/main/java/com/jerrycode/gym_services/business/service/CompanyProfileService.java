package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.CompanyProfileRepository;
import com.jerrycode.gym_services.data.vo.CompanyProfile;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.CompanyProfileRequest;
import com.jerrycode.gym_services.response.CompanyProfileResponse;
import com.jerrycode.gym_services.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CompanyProfileService implements CompanyProfileManager {

    private static final Logger logger = LoggerFactory.getLogger(CompanyProfileService.class);
    private final CompanyProfileRepository companyProfileRepository;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public Response<CompanyProfileResponse> updateOrCreateCompanyProfile(CompanyProfileRequest request, MultipartFile logo) {
        Response<CompanyProfileResponse> response = new Response<>();
        try {
            logger.info("Processing update or create company profile request {}" ,request);

            // Validate request
            if (request.getCompany_name() == null || request.getCompany_name().isEmpty()) {
                logger.warn("Invalid company profile request: Name is required");
                return Response.<CompanyProfileResponse>builder()
                        .success(false)
                        .message("Company name is required")
                        .build();
            }

            CompanyProfile companyProfile = companyProfileRepository.findById(request.getId())
                    .orElseThrow(() -> new EntityNotFoundException("CompanyProfile not found"));

            companyProfile.setCompanyName(request.getCompany_name());
            companyProfile.setAddress(request.getAddress());
            companyProfile.setPhone(request.getPhone());
            companyProfile.setCompanyEmail(request.getCompany_email());
            companyProfile.setDescription(request.getDescription());
            companyProfile.setWebsite(request.getWebsite());
            companyProfile.setPhone(request.getPhone());
            companyProfile.setAccountName(request.getAccount_name());
            companyProfile.setAccountNumber(request.getAccount_number());
            companyProfile.setFounder(request.getFounder());
            companyProfile.setManager(request.getManager());
            companyProfile.setTin(request.getTin());

            // Handle logo upload
            if (logo != null && !logo.isEmpty()) {
                // Validate file type and size
                if (!Arrays.asList("image/jpeg", "image/png").contains(logo.getContentType())) {
                    logger.warn("Invalid logo file type: {}", logo.getContentType());
                    return Response.<CompanyProfileResponse>builder()
                            .success(false)
                            .message("Invalid file type. Only JPEG or PNG allowed")
                            .build();
                }
                if (logo.getSize() > 5 * 1024 * 1024) { // 5MB limit
                    logger.warn("Logo file size exceeds limit: {} bytes", logo.getSize());
                    return Response.<CompanyProfileResponse>builder()
                            .success(false)
                            .message("Logo file size exceeds 5MB limit")
                            .build();
                }

                // Delete old logo if exists
                if (companyProfile.getLogo() != null) {
                    try {
                        fileStorageService.deleteLogo(companyProfile.getLogo());
                        logger.info("Deleted old logo: {}", companyProfile.getLogo());
                    } catch (IOException e) {
                        logger.warn("Failed to delete old logo: {}", e.getMessage(),e);
                    }
                }

                // Save new logo
                String logoPath = fileStorageService.storeLogo(logo);
                companyProfile.setLogo(logoPath);
                logger.info("Uploaded new logo with path: {}", logoPath);
            }

            // Save profile
            companyProfile = companyProfileRepository.save(companyProfile);
            CompanyProfileResponse responseData = modelMapper.map(companyProfile, CompanyProfileResponse.class);

            // Set logo URL if exists
            if (companyProfile.getLogo() != null) {
                responseData.setLogoUrl(fileStorageService.getLogoUrl(companyProfile.getLogo()));
            }

            response.setSuccess(true);
            response.setMessage("Company profile updated successfully");
            response.setData(responseData);
            logger.info("Company profile updated successfully");
        } catch (IOException e) {
            logger.error("Failed to store company logo: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Failed to store company logo");
        } catch (Exception e) {
            logger.error("Unexpected error updating company profile: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Processed update or create company profile response {}",response);
        return response;
    }

    @Override
    public Response<CompanyProfileResponse> getCompanyProfile() {
        Response<CompanyProfileResponse> response = new Response<>();
        try {
            logger.info("Fetching company profile");
            CompanyProfile companyProfile = companyProfileRepository.findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
            CompanyProfileResponse responseData = modelMapper.map(companyProfile, CompanyProfileResponse.class);

            if (companyProfile.getLogo() != null) {
                logger.debug("Original logo path from DB: {}", companyProfile.getLogo());
                String logoUrl = baseUrl + fileStorageService.getLogoUrl(companyProfile.getLogo());
                logger.info("Generated Image URL: {}", logoUrl);  // Add this line
                logger.info("Physical Path: {}", Paths.get(fileStorageService.getFullStoragePath(),
                        companyProfile.getLogo()));
                responseData.setLogoUrl(logoUrl);
            }

            response.setSuccess(true);
            response.setMessage("Company profile retrieved successfully");
            response.setData(responseData);
            logger.info("Company profile retrieved successfully");
        } catch (ResourceNotFoundException e) {
            logger.warn("Company profile not found: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving company profile: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Get company profile response {}",response);
        return response;
    }


    @Override
    public Response<CompanyProfileResponse> deleteCompanyProfile() {
        Response<CompanyProfileResponse> response = new Response<>();
        try {
            logger.info("Deleting company profile");
            CompanyProfile companyProfile = companyProfileRepository.findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

            // Delete logo file if exists
            if (companyProfile.getLogo() != null) {
                Files.deleteIfExists(Paths.get(companyProfile.getLogo()));
                logger.info("Deleted company logo: {}", companyProfile.getLogo());
            }

            companyProfileRepository.delete(companyProfile);
            response.setSuccess(true);
            response.setMessage("Company profile deleted successfully");
            logger.info("Company profile deleted successfully");
        } catch (ResourceNotFoundException e) {
            logger.warn("Company profile not found: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to delete company logo: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Failed to delete company logo");
        } catch (Exception e) {
            logger.error("Unexpected error deleting company profile: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Delete company profile response {}",response);
        return response;
    }
}