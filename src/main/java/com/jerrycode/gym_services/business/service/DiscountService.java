package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.DiscountRepository;
import com.jerrycode.gym_services.data.dao.PackageRepository;
import com.jerrycode.gym_services.data.vo.Discount;
import com.jerrycode.gym_services.data.vo.Packages;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.DiscountRequest;
import com.jerrycode.gym_services.response.DiscountResponse;
import com.jerrycode.gym_services.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountService implements DiscountManager {

    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);
    private final DiscountRepository discountRepository;
    private final PackageRepository packageRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<DiscountResponse> addDiscount(DiscountRequest request) {
        Response<DiscountResponse> response = new Response<>();
        try {
            logger.info("Processing add discount request {}",request);

            // Validate request
            if (request.getName() == null) {
                logger.warn("Invalid discount request: Name is required");
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount name is required")
                        .build();
            }
            if (request.getPercentage() <= 0 || request.getPercentage() > 100) {
                logger.warn("Invalid discount percentage: {}", request.getPercentage());
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount percentage must be between 1 and 100")
                        .build();
            }
            if (discountRepository.existsByName(request.getName())) {
                logger.warn("Discount name already exists: {}", request.getName());
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount name already exists")
                        .build();
            }

            // Map and save discount
            Discount discount = modelMapper.map(request, Discount.class);
            discount.setActive(true); // Default to active
            discount = discountRepository.save(discount);
            DiscountResponse responseData = modelMapper.map(discount, DiscountResponse.class);

            response.setSuccess(true);
            response.setMessage("Discount added successfully");
            response.setData(responseData);
            logger.info("Discount added successfully: {}", responseData.getName());
        } catch (Exception e) {
            logger.error("Unexpected error adding discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Adding discount response {}",response);
        return response;
    }

    @Override
    public Response<Map<String, Double>> calculateDiscount(Long packageId, Long discountId) {
        Response<Map<String, Double>> response = new Response<>();
        try {
            logger.info("Calculating discount for packageId: {}, discountId: {}", packageId, discountId);

            // Validate package
            Packages pkg = packageRepository.findById(packageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Package not found with ID: " + packageId));

            double discountPercentage = 0;
            if (discountId != null) {
                // Validate discount
                Discount discount = discountRepository.findById(discountId)
                        .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + discountId));
                if (!discount.isActive()) {
                    logger.warn("Discount is inactive: {}", discountId);
                    return Response.<Map<String, Double>>builder()
                            .success(false)
                            .message("Discount is inactive")
                            .build();
                }
                discountPercentage = discount.getPercentage();
            }

            // Calculate prices
            double discountFactor = 1 - (discountPercentage / 100.0);
            double totalPriceUSD = pkg.getPriceUSD() * discountFactor;
            double totalPriceTZS = pkg.getPriceTZS() * discountFactor;

            Map<String, Double> result = new HashMap<>();
            result.put("totalPriceUSD", totalPriceUSD);
            result.put("totalPriceTZS", totalPriceTZS);

            response.setSuccess(true);
            response.setMessage("Discount calculated successfully");
            response.setData(result);
            logger.info("Discount calculated successfully for packageId: {}", packageId);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error calculating discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error calculating discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Calculate discount response {}",response);
        return response;
    }

    @Override
    public Response<List<DiscountResponse>> getAllDiscounts() {
        Response<List<DiscountResponse>> response = new Response<>();
        try {
            logger.info("Fetching all discounts");
            List<DiscountResponse> discounts = discountRepository.findAll().stream()
                    .map(discount -> modelMapper.map(discount, DiscountResponse.class))
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Discounts retrieved successfully");
            response.setData(discounts);
            logger.info("Retrieved {} discounts", discounts.size());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving discounts: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting all discounts response {}",response);
        return response;
    }

    @Override
    public Response<List<DiscountResponse>> getActiveDiscounts() {
        Response<List<DiscountResponse>> response = new Response<>();
        try {
            logger.info("Fetching active discounts");
            List<DiscountResponse> discounts = discountRepository.findByActiveTrue().stream()
                    .map(discount -> modelMapper.map(discount, DiscountResponse.class))
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Active discounts retrieved successfully");
            response.setData(discounts);
            logger.info("Retrieved {} active discounts", discounts.size());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving active discounts: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting active discounts response {}",response);
        return response;
    }

    @Override
    public Response<DiscountResponse> updateDiscount(Long id, DiscountRequest request) {
        Response<DiscountResponse> response = new Response<>();
        try {
            logger.info("Updating discount: {} with id: {}", request,id);

            // Validate request
            if (request.getName() == null || request.getName().isEmpty()) {
                logger.warn("Invalid discount request: Name is required");
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount name is required")
                        .build();
            }
            if (request.getPercentage() <= 0 || request.getPercentage() > 100) {
                logger.warn("Invalid discount percentage: {}", request.getPercentage());
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount percentage must be between 1 and 100")
                        .build();
            }
            if (!discountRepository.existsById(id)) {
                logger.warn("Discount not found: {}", id);
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount not found with ID: " + id)
                        .build();
            }
            if (discountRepository.existsByNameAndIdNot(request.getName(), id)) {
                logger.warn("Discount name already exists: {}", request.getName());
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount name already exists")
                        .build();
            }

            // Update discount
            Discount discount = discountRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + id));
            modelMapper.map(request, discount);
            discount = discountRepository.save(discount);
            DiscountResponse responseData = modelMapper.map(discount, DiscountResponse.class);

            response.setSuccess(true);
            response.setMessage("Discount updated successfully");
            response.setData(responseData);
            logger.info("Discount updated successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error updating discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Updating discount response {}",response);
        return response;
    }

    @Override
    public Response<DiscountResponse> toggleDiscountStatus(Long id) {
        Response<DiscountResponse> response = new Response<>();
        try {
            logger.info("Toggling discount status for id: {}", id);
            Discount discount = discountRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Discount not found with ID: " + id));
            discount.setActive(!discount.isActive());
            discount = discountRepository.save(discount);
            DiscountResponse responseData = modelMapper.map(discount, DiscountResponse.class);

            response.setSuccess(true);
            response.setMessage("Discount status toggled successfully");
            response.setData(responseData);
            logger.info("Discount status toggled for id: {}", id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error toggling discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error toggling discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Toggle response for discount {}",response);
        return response;
    }

    @Override
    public Response<DiscountResponse> deleteDiscount(Long id) {
        Response<DiscountResponse> response = new Response<>();
        try {
            logger.info("Deleting discount with id: {}", id);
            if (!discountRepository.existsById(id)) {
                logger.warn("Discount not found: {}", id);
                return Response.<DiscountResponse>builder()
                        .success(false)
                        .message("Discount not found with ID: " + id)
                        .build();
            }
            discountRepository.deleteById(id);

            response.setSuccess(true);
            response.setMessage("Discount deleted successfully");
            logger.info("Discount deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Unexpected error deleting discount: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Deleted discount response response {}",response);
        return response;
    }
}