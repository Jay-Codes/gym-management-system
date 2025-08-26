package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.PackageManager;
import com.jerrycode.gym_services.request.PackageRequest;
import com.jerrycode.gym_services.response.PackagesResponse;
import com.jerrycode.gym_services.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api")
public class PackageController {

    private static final Logger logger = LoggerFactory.getLogger(PackageController.class);
    private final PackageManager packageManager;

    public PackageController(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @PostMapping("/package")
    public ResponseEntity<Response<PackagesResponse>> addPackage(@Valid @RequestBody PackageRequest request) {
        logger.info("Processing add package request: {}" ,request);
        Response<PackagesResponse> response = packageManager.addPackage(request);
        logger.info("Processed add package response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/all-packages")
    public ResponseEntity<Response<List<PackagesResponse>>> getAllPackages() {
        logger.info("Getting all packages");
        Response<List<PackagesResponse>> response = packageManager.getAllPackages();
        logger.info("Fetched packages response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/package/{id}")
    public ResponseEntity<Response<PackagesResponse>> getPackageById(@PathVariable @Positive Long id) {
        logger.info("Fetching package with id: {}", id);
        Response<PackagesResponse> response = packageManager.getPackageById(id);
        logger.info("Fetched package response {}",response);
        return buildResponseEntity(response);
    }

    @PutMapping("/package/{id}/update")
    public ResponseEntity<Response<PackagesResponse>> updatePackage(
            @PathVariable @Positive Long id,
            @Valid @RequestBody PackageRequest request) {
        logger.info("Updating package: {} with id: {}", request,id);
        Response<PackagesResponse> response = packageManager.updatePackage(id, request);
        logger.info("Updated [ackage response {}",response);
        return buildResponseEntity(response);
    }

    @DeleteMapping("/package/{id}/delete")
    public ResponseEntity<Response<PackagesResponse>> deletePackage(@PathVariable @Positive Long id) {
        logger.info("Deleting package with id: {}", id);
        Response<PackagesResponse> response = packageManager.deletePackage(id);
        logger.info("Deleted package response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/total-packages")
    public ResponseEntity<Response<Long>> getTotalPackages() {
        logger.info("Fetching total number of packages");
        Response<Long> response = packageManager.getTotalPackages();
        logger.info("Fetched total packages response {}",response);
        return buildResponseEntity(response);
    }

}