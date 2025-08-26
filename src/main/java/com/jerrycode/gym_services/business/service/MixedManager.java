package com.jerrycode.gym_services.business.service;


import com.jerrycode.gym_services.response.AllCountsResponse;
import com.jerrycode.gym_services.response.Response;

public interface MixedManager {
    Response<AllCountsResponse> fetchAllCounts();
}
