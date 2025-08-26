package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.request.DiscountRequest;
import com.jerrycode.gym_services.response.DiscountResponse;
import com.jerrycode.gym_services.response.Response;

import java.util.List;
import java.util.Map;

public interface DiscountManager {
    Response<DiscountResponse> addDiscount(DiscountRequest request);
    Response<Map<String, Double>> calculateDiscount(Long packageId, Long discountId);
    Response<List<DiscountResponse>> getAllDiscounts();
    Response<List<DiscountResponse>> getActiveDiscounts();
    Response<DiscountResponse> updateDiscount(Long id, DiscountRequest request);
    Response<DiscountResponse> toggleDiscountStatus(Long id);
    Response<DiscountResponse> deleteDiscount(Long id);
}
