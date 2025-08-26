package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.MixedManager;
import com.jerrycode.gym_services.response.AllCountsResponse;
import com.jerrycode.gym_services.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api/totals")
public class MixedController {

    private static final Logger logger = LoggerFactory.getLogger(MixedController.class);
    private final MixedManager mixedManager;

    public MixedController(MixedManager mixedManager) {
        this.mixedManager = mixedManager;
    }

    @GetMapping
    public ResponseEntity<Response<AllCountsResponse>> fetchAllCounts() {
        logger.info("Fetching all counts");
        Response<AllCountsResponse> response = mixedManager.fetchAllCounts();
        logger.info("Fetched all counts response {}",response);
        return buildResponseEntity(response);
    }

}