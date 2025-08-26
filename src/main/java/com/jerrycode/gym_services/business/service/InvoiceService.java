package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.CompanyProfileRepository;
import com.jerrycode.gym_services.data.dao.InvoiceRepository;
import com.jerrycode.gym_services.data.dao.MemberRepository;
import com.jerrycode.gym_services.data.dao.PackageRepository;
import com.jerrycode.gym_services.data.vo.CompanyProfile;
import com.jerrycode.gym_services.data.vo.Invoices;
import com.jerrycode.gym_services.data.vo.Member;
import com.jerrycode.gym_services.data.vo.Packages;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.InvoiceRequest;
import com.jerrycode.gym_services.response.InvoiceResponse;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.utils.PDFGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService implements InvoiceManager {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    private final InvoiceRepository invoiceRepository;
    private final MemberRepository memberRepository;
    private final PackageRepository packageRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ModelMapper modelMapper;
    private final SmsService smsService;
    private final PDFGenerator pdfGenerator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    @Override
    public Response<InvoiceResponse> createInvoice(InvoiceRequest request) {
        Response<InvoiceResponse> response = new Response<>();
        try {
            logger.info("Processing create invoice request: {}", request);

            // Validate request
            if (request == null) {
                logger.warn("Invalid invoice request: Request is null");
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(Collections.singletonMap("general", Collections.singletonList("Request cannot be null")));
                return response;
            }

            // Check for missing required fields and invalid dates
            Map<String, List<String>> errors = new HashMap<>();
            if (request.getMemberId() == null) {
                errors.put("member_id", Collections.singletonList("The member ID is required."));
            }
            if (request.getPackageName() == null || request.getPackageName().isEmpty()) {
                errors.put("package_name", Collections.singletonList("The package name is required."));
            }
            if (request.getStartDate() == null || request.getStartDate().isEmpty()) {
                errors.put("start_date", Collections.singletonList("The start date is required."));
            }
            if (request.getEndDate() == null || request.getEndDate().isEmpty()) {
                errors.put("end_date", Collections.singletonList("The end date is required."));
            }

            // Validate date formats
            LocalDate startDate = null;
            LocalDate endDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
                try {
                    startDate = LocalDate.parse(request.getStartDate(), formatter);
                } catch (DateTimeParseException e) {
                    errors.put("start_date", Collections.singletonList("The start date must be a valid date in yyyy-MM-dd format."));
                }
            }
            if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
                try {
                    endDate = LocalDate.parse(request.getEndDate(), formatter);
                } catch (DateTimeParseException e) {
                    errors.put("end_date", Collections.singletonList("The end date must be a valid date in yyyy-MM-dd format."));
                }
            }

            // If there are any validation errors, return them
            if (!errors.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(errors);
                logger.warn("Validation failed for create invoice: {}", errors);
                return response;
            }

            // Validate member
            Member member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + request.getMemberId()));

            // Validate package
            Packages pkg = packageRepository.findByName(request.getPackageName())
                    .orElseThrow(() -> new ResourceNotFoundException("Package not found with name: " + request.getPackageName()));

            // Validate company profile
            CompanyProfile company = companyProfileRepository.findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

            // Map and save invoice
            Invoices invoice = modelMapper.map(request, Invoices.class);
            invoice.setMemberId(member.getId());
            invoice.setPackageName(pkg.getName());
            invoice.setStartDate(startDate); // Set parsed LocalDate
            invoice.setEndDate(endDate);     // Set parsed LocalDate

            // update Member details
            member.setSubscribedStartDate(invoice.getStartDate().atStartOfDay());
            member.setSubscribedEndDate(invoice.getEndDate().atStartOfDay());
            member.setPackageSubscribed(invoice.getPackageName());
            member.setPackagePaidAmount(invoice.getAmountPaid());
            try {
                // Save the invoice
                invoice = invoiceRepository.save(invoice);

                // Save the member details
                member = memberRepository.save(member);
                logger.info("Customer invoice {} for member {}",invoice,member);

                    // Check SMS balance before sending
                boolean smsStatus  = smsService.checkProviderSmsBalance();
                if (smsStatus){
                    smsService.sendPaymentConfirmationSms(member, invoice, pkg,company);
                }

            } catch (Exception e) {
                // Handle save failure
                logger.error("Failed to save invoice to database: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save invoice to database");
            }

            // Map to InvoiceResponse
            InvoiceResponse responseData = modelMapper.map(invoice, InvoiceResponse.class);

            // Prepare success response
            response.setSuccess(true);
            response.setMessage("Invoice created successfully");
            response.setData(responseData);
            logger.info("Invoice created successfully: {}", responseData.getId());

        } catch (ResourceNotFoundException e) {
            // Handle resource not found
            logger.warn("Error creating invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (RuntimeException e) {
            // Handle database or other runtime errors
            logger.error("Runtime error creating invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error creating invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList("An unexpected error occurred while creating the invoice. Please try again.")));
        }
        logger.info("Create invoice process response {}",response);
        return response;
    }

    @Override
    public byte[] generateInvoiceReport(Long id) throws ResourceNotFoundException {
        logger.info("Generating invoice report for id: {}", id);

        // Validate invoice
        Invoices invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

        // Validate company profile
        CompanyProfile company = companyProfileRepository.findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));

        // Generate and return PDF bytes directly
        return pdfGenerator.generateInvoicePdf(invoice, company);
    }

    @Override
    public Response<List<InvoiceResponse>> getAllInvoices() {
        Response<List<InvoiceResponse>> response = new Response<>();
        try {
            logger.info("Fetching all invoices");

            // Fetch invoices sorted in descending order by 'createdAt'
            List<Invoices> invoices = invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            Set<Long> memberIds = invoices.stream()
                    .map(Invoices::getMemberId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Member> membersMap = memberRepository.findAllById(memberIds).stream()
                    .collect(Collectors.toMap(Member::getId, member -> member));

            List<InvoiceResponse> invoiceResponses = invoices.stream().map(invoice -> {
                Member member = membersMap.get(invoice.getMemberId());

                return InvoiceResponse.builder()
                        .id(invoice.getId())
                        .userName(invoice.getUserName())
                        .userPhone(invoice.getUserPhone())
                        .userEmail(invoice.getUserEmail())
                        .memberId(member != null ? member.getId() : null)
                        .memberName(member != null ? member.getName() : null)
                        .memberPhone(member != null ? member.getPhoneNumber() : null)
                        .amountPaid(String.format("%.2f", invoice.getAmountPaid()))
                        .status(invoice.getStatus())
                        .paid(1)
                        .memo(invoice.getMemo())
                        .packageName(invoice.getPackageName())
                        .discountPercentage(String.format("%.2f", invoice.getDiscountPercentage()))
                        .startDate(invoice.getStartDate().format(DATE_FORMATTER))
                        .endDate(invoice.getEndDate().format(DATE_FORMATTER))
                        .invoiceFile(invoice.getInvoiceFile())
                        .createdAt(invoice.getCreatedAt().format(DATE_TIME_FORMATTER))
                        .updatedAt(invoice.getUpdatedAt().format(DATE_TIME_FORMATTER))
                        .build();
            }).collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Invoices retrieved successfully");
            response.setData(invoiceResponses);
            logger.info("Retrieved {} invoices", invoiceResponses.size());

        } catch (Exception e) {
            logger.error("Unexpected error retrieving invoices: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        return response;
    }

    @Override
    public Response<List<InvoiceResponse>> getDailyInvoices() {
        Response<List<InvoiceResponse>> response = new Response<>();
        try {
            logger.info("Fetching daily invoices");

            LocalDate today = LocalDate.now();
            List<Invoices> dailyInvoices = invoiceRepository.findByCreatedAtBetween(
                    today.atStartOfDay(), today.plusDays(1).atStartOfDay()
            );

            Set<Long> memberIds = dailyInvoices.stream()
                    .map(Invoices::getMemberId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Member> membersMap = memberRepository.findAllById(memberIds).stream()
                    .collect(Collectors.toMap(Member::getId, member -> member));

            List<InvoiceResponse> invoiceResponses = dailyInvoices.stream().map(invoice -> {
                Member member = membersMap.get(invoice.getMemberId());

                return InvoiceResponse.builder()
                        .id(invoice.getId())
                        .userName(invoice.getUserName())
                        .userPhone(invoice.getUserPhone())
                        .userEmail(invoice.getUserEmail())
                        .memberId(member != null ? member.getId() : null)
                        .memberName(member != null ? member.getName() : null)
                        .memberPhone(member != null ? member.getPhoneNumber() : null)
                        .amountPaid(String.format("%.2f", invoice.getAmountPaid()))
                        .status(invoice.getStatus())
                        .paid(1)
                        .memo(invoice.getMemo())
                        .packageName(invoice.getPackageName())
                        .discountPercentage(String.format("%.2f", invoice.getDiscountPercentage()))
                        .startDate(invoice.getStartDate().format(DATE_FORMATTER))
                        .endDate(invoice.getEndDate().format(DATE_FORMATTER))
                        .invoiceFile(invoice.getInvoiceFile())
                        .createdAt(invoice.getCreatedAt().format(DATE_TIME_FORMATTER))
                        .updatedAt(invoice.getUpdatedAt().format(DATE_TIME_FORMATTER))
                        .build();
            }).collect(Collectors.toList());

            response.setSuccess(true);
            response.setMessage("Daily invoices retrieved successfully");
            response.setData(invoiceResponses);
            logger.info("Retrieved {} daily invoices", invoiceResponses.size());

        } catch (Exception e) {
            logger.error("Unexpected error retrieving daily invoices: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred while fetching daily invoices");
        }
        return response;
    }



    @Override
    public Response<InvoiceResponse> getInvoiceById(Long id) {
        Response<InvoiceResponse> response = new Response<>();
        try {
            logger.info("Fetching invoice with id: {}", id);
            Invoices invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
            InvoiceResponse responseData = modelMapper.map(invoice, InvoiceResponse.class);

            response.setSuccess(true);
            response.setMessage("Invoice retrieved successfully");
            response.setData(responseData);
            logger.info("Invoice retrieved successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            logger.warn("Error retrieving invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Response for invoice fetched {}",response);
        return response;
    }

    @Override
    public Response<InvoiceResponse> updateInvoice(Long id, InvoiceRequest request) {
        Response<InvoiceResponse> response = new Response<>();
        try {
            logger.info("Updating invoice memo with id: {} and request: {}", id, request);

            // Validate request
            if (request == null) {
                logger.warn("Invalid invoice request: Request is null");
                response.setSuccess(false);
                response.setMessage("Validation failed");
                response.setErrors(Collections.singletonMap("general", Collections.singletonList("Request cannot be null")));
                return response;
            }

            // Validate existence of the invoice
            Invoices invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));

            // Update memo (allow null/empty to clear it)
            invoice.setMemo(request.getMemo());

            // Save the updated invoice
            try {
                invoice = invoiceRepository.save(invoice);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save updated invoice to database");
            }

            // Map to InvoiceResponse
            InvoiceResponse responseData = modelMapper.map(invoice, InvoiceResponse.class);

            // Prepare success response
            response.setSuccess(true);
            response.setMessage("Invoice memo updated successfully");
            response.setData(responseData);
            logger.info("Invoice memo updated successfully: {}", id);

        } catch (ResourceNotFoundException e) {
            // Handle invoice not found
            logger.warn("Error updating invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (RuntimeException e) {
            // Handle database or other runtime errors
            logger.error("Runtime error updating invoice with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList(e.getMessage())));

        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Unexpected error updating invoice with ID {}: {}", id, e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setErrors(Collections.singletonMap("general", Collections.singletonList("An unexpected error occurred while updating the invoice. Please try again.")));
        }
        logger.info("Update invoice response {}",response);
        return response;
    }

    @Override
    public Response<InvoiceResponse> deleteInvoice(Long id) {
        Response<InvoiceResponse> response = new Response<>();
        try {
            logger.info("Deleting invoice with id: {}", id);
            if (!invoiceRepository.existsById(id)) {
                logger.warn("Invoice not found: {}", id);
                return Response.<InvoiceResponse>builder()
                        .success(false)
                        .message("Invoice not found with ID: " + id)
                        .build();
            }
            invoiceRepository.deleteById(id);

            response.setSuccess(true);
            response.setMessage("Invoice deleted successfully");
            logger.info("Invoice deleted successfully: {}", id);
        } catch (Exception e) {
            logger.error("Unexpected error deleting invoice: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Deleted Invoice response {}",response);
        return response;
    }

    @Override
    public Response<Long> getTotalInvoices() {
        Response<Long> response = new Response<>();
        try {
            logger.info("Fetching total number of invoices");
            Long total = invoiceRepository.count();

            response.setSuccess(true);
            response.setMessage("Total invoices retrieved successfully");
            response.setData(total);
            logger.info("Retrieved total invoices: {}", total);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving total invoices: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting total invoices response {}",response);
        return response;
    }

    @Override
    public Response<Long> getTotalDiscounts() {
        Response<Long> response = new Response<>();
        try {
            logger.info("Fetching total number of discounts applied");
            Long total = invoiceRepository.countByDiscountPercentageGreaterThan(0);

            response.setSuccess(true);
            response.setMessage("Total discounts retrieved successfully");
            response.setData(total);
            logger.info("Retrieved total discounts: {}", total);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving total discounts: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Getting total discounts response {}",response);
        return response;
    }
}
