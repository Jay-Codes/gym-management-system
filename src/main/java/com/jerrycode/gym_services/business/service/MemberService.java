package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.CompanyProfileRepository;
import com.jerrycode.gym_services.data.dao.MemberRepository;
import com.jerrycode.gym_services.data.vo.CompanyProfile;
import com.jerrycode.gym_services.data.vo.Member;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.MemberRequest;
import com.jerrycode.gym_services.response.MemberResponse;
import com.jerrycode.gym_services.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService implements MemberManager {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final SmsService smsService;
    private final CompanyProfileRepository companyProfileRepository;

    @Override
    public Response<MemberResponse> addMember(MemberRequest request) {
        Response<MemberResponse> response = new Response<>();
        try {
            logger.info("Processing add member request: {}", request);

            // Validate request
            if (request == null) {
                logger.warn("Invalid member request: Request is null");
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(Collections.singletonMap("general", Collections.singletonList("Request cannot be null")));
                return response;
            }

            // Check for missing required fields and duplicates
            Map<String, List<String>> errors = new HashMap<>();
            if (request.getName() == null || request.getName().isEmpty()) {
                errors.put("name", Collections.singletonList("The name is required."));
            }

            // Handle email (optional but unique if provided)
            String email = request.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                // Check uniqueness only if email is non-empty
                if (memberRepository.existsByEmail(request.getEmail())) {
                    errors.put("email", Collections.singletonList("Email already exists."));
                }
            } else {
                // Convert empty email to NULL
                request.setEmail(null);
            }

            // Phone number validation
            if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
                errors.put("phone_number", Collections.singletonList("The phone number is required."));
            } else if (memberRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                errors.put("phone_number", Collections.singletonList("The phone number has already been taken."));
            }

            // If there are any errors, return them
            if (!errors.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(errors);
                logger.warn("Validation failed for add member: {}", errors);
                return response;
            }

            // Map and save member
            Member member = modelMapper.map(request, Member.class);
            try {
                // Save the member
                member = memberRepository.save(member);

                CompanyProfile company = companyProfileRepository.findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

                // Check SMS balance before sending
                boolean smsStatus  = smsService.checkProviderSmsBalance();
                if (smsStatus){
                    smsService.sendWelcomeSms(member,company);
                }
            } catch (Exception e) {
                // Handle save failure
                logger.error("Failed to save member to database: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save member to database");
            }

            // Map to MemberResponse
            MemberResponse responseData = modelMapper.map(member, MemberResponse.class);

            // Prepare success response
            response.setSuccess(true);
            response.setMessage("Member added successfully");
            response.setData(responseData);
            logger.info("Member added successfully: {}", responseData.getId());

        } catch (RuntimeException e) {
            // Handle database or other runtime errors
            logger.error("Runtime error adding member: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error adding member: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList("An unexpected error occurred while adding the member. Please try again.")));
        }
        logger.info("Add member Processed Response {}",response);
        return response;
    }


    @Override
    public Response<List<MemberResponse>> getAllMembers() {
        Response<List<MemberResponse>> response = new Response<>();
        try {
            logger.info("Fetching all members");
            List<MemberResponse> members = memberRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                    .map(member -> modelMapper.map(member, MemberResponse.class))
                    .collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Members retrieved successfully");
            response.setData(members);
            logger.info("Retrieved {} members", members.size());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving members: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("All members fetched response");
        return response;
    }

    @Override
    public Response<MemberResponse> getMemberById(Long id) {
        Response<MemberResponse> response = new Response<>();
        try {
            logger.info("Fetching member with id: {}", id);
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
            MemberResponse responseData = modelMapper.map(member, MemberResponse.class);

            response.setSuccess(true);
            response.setMessage("Member retrieved successfully");
            response.setData(responseData);
            logger.info("Member retrieved successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error retrieving member: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving member: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Fetched member response {}",response);
        return response;
    }

    @Override
    public Response<MemberResponse> updateMember(Long id, MemberRequest request) {
        Response<MemberResponse> response = new Response<>();
        try {
            logger.info("Updating member: {} with id: {}", request,id);

            // Validate request
            if (request == null) {
                logger.warn("Invalid member request: Request is null");
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(Collections.singletonMap("general", Collections.singletonList("Request cannot be null")));
                return response;
            }

            // Validate member exists
            Member member = memberRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));

            // Check for missing required fields and duplicates
            Map<String, List<String>> errors = new HashMap<>();
            if (request.getName() != null && !request.getName().equals(member.getName()) && memberRepository.existsByNameAndIdNot(request.getName(), id)) {
                errors.put("name", Collections.singletonList("The name has already been taken."));
            }
            if (request.getEmail() != null && member.getEmail() != null &&
                    (!request.getEmail().equals(member.getEmail()) &&
                            memberRepository.existsByEmailAndIdNot(request.getEmail(), id))) {
                errors.put("email", Collections.singletonList("The email has already been taken."));
            }
            if (!request.getPhoneNumber().equals(member.getPhoneNumber()) && memberRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
                errors.put("phone_number", Collections.singletonList("The phone number has already been taken."));
            }

            // If there are any errors, return them
            if (!errors.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(errors);
                logger.warn("Validation failed for member update ID {}: {}", id, errors);
                return response;
            }

            // Update member
            modelMapper.map(request, member);
            try {
                member = memberRepository.save(member);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save updated member to database");
            }

            // Map to MemberResponse
            MemberResponse responseData = modelMapper.map(member, MemberResponse.class);

            // Prepare success response
            response.setSuccess(true);
            response.setMessage("Member updated successfully");
            response.setData(responseData);
            logger.info("Member updated successfully: {}", id);

        } catch (ResourceNotFoundException e) {
            // Handle member not found
            logger.warn("Error updating member: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (RuntimeException e) {
            // Handle database or other runtime errors
            logger.error("Runtime error updating member with ID {}: {}", id, e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error updating member with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList("An unexpected error occurred while updating the member. Please try again.")));
        }
        logger.info("Update member response back {}",response);
        return response;
    }

    @Override
    public Response<MemberResponse> deleteMember(Long id) {
        Response<MemberResponse> response = new Response<>();
        try {
            logger.info("Deleting member with id: {}", id);
            if (!memberRepository.existsById(id)) {
                logger.warn("Member not found: {}", id);
                return Response.<MemberResponse>builder()
                        .success(false)
                        .message("Member not found with ID: " + id)
                        .build();
            }
            memberRepository.deleteById(id);

            response.setSuccess(true);
            response.setMessage("Member deleted successfully");
            logger.info("Member deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Unexpected error deleting member: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Delete member response back {}",response);
        return response;
    }

    @Override
    public Response<Long> getTotalMembers() {
        Response<Long> response = new Response<>();
        try {
            logger.info("Fetching total number of members");
            Long total = memberRepository.count();

            response.setSuccess(true);
            response.setMessage("Total members retrieved successfully");
            response.setData(total);
            logger.info("Retrieved total members: {}", total);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving total members: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Total members response back {}",response);
        return response;
    }
}