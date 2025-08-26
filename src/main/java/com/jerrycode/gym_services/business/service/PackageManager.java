package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.request.PackageRequest;
import com.jerrycode.gym_services.response.PackagesResponse;
import com.jerrycode.gym_services.response.Response;

import java.util.List;

public interface PackageManager {
    Response<PackagesResponse> addPackage(PackageRequest request);
    Response<List<PackagesResponse>> getAllPackages();
    Response<PackagesResponse> getPackageById(Long id);
    Response<PackagesResponse> updatePackage(Long id, PackageRequest request);
    Response<PackagesResponse> deletePackage(Long id);
    Response<Long> getTotalPackages();
}
