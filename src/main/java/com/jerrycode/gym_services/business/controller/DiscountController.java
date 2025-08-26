package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.DiscountManager;
import com.jerrycode.gym_services.request.DiscountRequest;
import com.jerrycode.gym_services.response.DiscountResponse;
import com.jerrycode.gym_services.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api")
public class DiscountController {

    private static final Logger logger = LoggerFactory.getLogger(DiscountController.class);
    private final DiscountManager discountManager;

    public DiscountController(DiscountManager discountManager) {
        this.discountManager = discountManager;
    }

    @PostMapping("/discount")
    public ResponseEntity<Response<DiscountResponse>> addDiscount(@Valid @RequestBody DiscountRequest request) {
        logger.info("Processing add discount request {}" ,request);
        Response<DiscountResponse> response = discountManager.addDiscount(request);
        logger.info("Processing add Discount Response {}",response);
        return buildResponseEntity(response);
    }

    @PostMapping("/calculate-discount")
    public ResponseEntity<Response<Map<String, Double>>> calculateDiscount(
            @RequestParam @Positive Long packageId,
            @RequestParam(required = false) Long discountId) {
        logger.info("Calculating discount for packageId {} discountId {}", packageId, discountId);
        Response<Map<String, Double>> response = discountManager.calculateDiscount(packageId, discountId);
        logger.info("Calculating Discount for Package Response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/all-discounts")
    public ResponseEntity<Response<List<DiscountResponse>>> getAllDiscounts() {
        logger.info("Getting all discounts");
        Response<List<DiscountResponse>> response = discountManager.getAllDiscounts();
        logger.info("Fetched Discounts Response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/active-discounts")
    public ResponseEntity<Response<List<DiscountResponse>>> getActiveDiscounts() {
        logger.info("Getting active discounts");
        Response<List<DiscountResponse>> response = discountManager.getActiveDiscounts();
        logger.info("Fetched Active Discounts Response {}",response);
        return buildResponseEntity(response);
    }

    @PutMapping("/discount/{id}/update")
    public ResponseEntity<Response<DiscountResponse>> updateDiscount(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DiscountRequest request) {
        logger.info("Updating discount {} with id {}", request,id);
        Response<DiscountResponse> response = discountManager.updateDiscount(id, request);
        logger.info("Updated Discount Response {}",response);
        return buildResponseEntity(response);
    }

    @PutMapping("/discount/{id}/toggle")
    public ResponseEntity<Response<DiscountResponse>> toggleDiscountStatus(@PathVariable @Positive Long id) {
        logger.info("Toggling discount status for id {}", id);
        Response<DiscountResponse> response = discountManager.toggleDiscountStatus(id);
        logger.info("Toggling Discount Status Response {}",response);
        return buildResponseEntity(response);
    }

    @DeleteMapping("/discount/{id}/delete")
    public ResponseEntity<Response<DiscountResponse>> deleteDiscount(@PathVariable @Positive Long id) {
        logger.info("Deleting discount with id {}", id);
        Response<DiscountResponse> response = discountManager.deleteDiscount(id);
        logger.info("Delete Discount Response {}",response);
        return buildResponseEntity(response);
    }

}