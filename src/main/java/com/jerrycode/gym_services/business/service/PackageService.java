package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.PackageRepository;
import com.jerrycode.gym_services.data.vo.Packages;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.PackageRequest;
import com.jerrycode.gym_services.response.PackagesResponse;
import com.jerrycode.gym_services.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PackageService implements PackageManager {

    private static final Logger logger = LoggerFactory.getLogger(PackageService.class);
    private final PackageRepository packageRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<PackagesResponse> addPackage(PackageRequest request) {
        Response<PackagesResponse> response = new Response<>();
        try {
            logger.info("Processing add package request: {}",request);

            // Validate request
            if (request.getName() == null || request.getName().isEmpty()) {
                logger.warn("Invalid package request: Name is required");
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Package name is required")
                        .build();
            }
            if (packageRepository.existsByName(request.getName())) {
                logger.warn("Package name already exists: {}", request.getName());
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Package name already exists")
                        .build();
            }
            if (request.getPriceTZS() <= 0) {
                logger.warn("Invalid package request: Valid TZS price is required");
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Valid TZS price is required")
                        .build();
            }

            // Map and save package
            Packages pkg = modelMapper.map(request, Packages.class);
            pkg = packageRepository.save(pkg);
            PackagesResponse responseData = modelMapper.map(pkg, PackagesResponse.class);

            response.setSuccess(true);
            response.setMessage("Package added successfully");
            response.setData(responseData);
            logger.info("Package added successfully: {}", responseData.getId());
        } catch (Exception e) {
            logger.error("Unexpected error adding package: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Adding package response back {}",response);
        return response;
    }

    @Override
    public Response<List<PackagesResponse>> getAllPackages() {
        Response<List<PackagesResponse>> response = new Response<>();
        try {
            logger.info("Fetching all packages");
            List<PackagesResponse> packages = packageRepository.findAll().stream()
                    .map(pkg -> modelMapper.map(pkg, PackagesResponse.class))
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Packages retrieved successfully");
            response.setData(packages);
            logger.info("Retrieved {} packages", packages.size());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving packages: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting all packages response back {}",response);
        return response;
    }

    @Override
    public Response<PackagesResponse> getPackageById(Long id) {
        Response<PackagesResponse> response = new Response<>();
        try {
            logger.info("Fetching package with id: {}", id);
            Packages pkg = packageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + id));
            PackagesResponse responseData = modelMapper.map(pkg, PackagesResponse.class);

            response.setSuccess(true);
            response.setMessage("Package retrieved successfully");
            response.setData(responseData);
            logger.info("Package retrieved successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error retrieving package: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving package: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting package response back {}",response);
        return response;
    }

    @Override
    public Response<PackagesResponse> updatePackage(Long id, PackageRequest request) {
        Response<PackagesResponse> response = new Response<>();
        try {
            logger.info("Updating package: {} with id: {}",request, id);

            // Validate request
            if (request.getName() == null || request.getName().isEmpty()) {
                logger.warn("Invalid package request: Name is required");
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Package name is required")
                        .build();
            }
            if (packageRepository.existsByNameAndIdNot(request.getName(), id)) {
                logger.warn("Package name already exists: {}", request.getName());
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Package name already exists")
                        .build();
            }
            if (request.getPriceTZS() <= 0) {
                logger.warn("Invalid package request: Valid TZS price is required");
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Valid TZS price is required")
                        .build();
            }

            // Validate package exists
            Packages pkg = packageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + id));

            // Update package
            modelMapper.map(request, pkg);
            pkg = packageRepository.save(pkg);
            PackagesResponse responseData = modelMapper.map(pkg, PackagesResponse.class);

            response.setSuccess(true);
            response.setMessage("Package updated successfully");
            response.setData(responseData);
            logger.info("Package updated successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error updating package: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating package: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Updating package response back {}",response);
        return response;
    }

    @Override
    public Response<PackagesResponse> deletePackage(Long id) {
        Response<PackagesResponse> response = new Response<>();
        try {
            logger.info("Deleting package with id: {}", id);
            if (!packageRepository.existsById(id)) {
                logger.warn("Package not found: {}", id);
                return Response.<PackagesResponse>builder()
                        .success(false)
                        .message("Package not found with ID: " + id)
                        .build();
            }
            packageRepository.deleteById(id);

            response.setSuccess(true);
            response.setMessage("Package deleted successfully");
            logger.info("Package deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Unexpected error deleting package: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Delete package response back {}",response);
        return response;
    }

    @Override
    public Response<Long> getTotalPackages() {
        Response<Long> response = new Response<>();
        try {
            logger.info("Fetching total number of packages");
            Long total = packageRepository.count();

            response.setSuccess(true);
            response.setMessage("Total packages retrieved successfully");
            response.setData(total);
            logger.info("Retrieved total packages: {}", total);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving total packages: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting total package response back {}",response);
        return response;
    }
}