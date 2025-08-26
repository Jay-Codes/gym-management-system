package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.InvoiceManager;
import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.InvoiceRequest;
import com.jerrycode.gym_services.response.ErrorResponse;
import com.jerrycode.gym_services.response.InvoiceResponse;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.utils.RoleCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
    private final InvoiceManager invoiceManager;

    public InvoiceController(InvoiceManager invoiceManager) {
        this.invoiceManager = invoiceManager;
    }

    @PostMapping("/create-invoice")
    public ResponseEntity<Response<InvoiceResponse>> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        logger.info("Processing create invoice request {}" ,request);
        Response<InvoiceResponse> response = invoiceManager.createInvoice(request);
        logger.info("Processing Create Invoice Response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/invoice-report/{id}/report")
    public ResponseEntity<?> generateInvoiceReport(@PathVariable @Positive Long id) {
        logger.info("Generating invoice report for id {}", id);

        try {
            byte[] pdfBytes = invoiceManager.generateInvoiceReport(id);

            // Return the PDF file directly
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice_report_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(new ByteArrayResource(pdfBytes));

        } catch (ResourceNotFoundException e) {
            logger.error("Invoice not found: {}", e.getMessage(), e);
            // Return JSON error response
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating PDF: {}", e.getMessage(), e);
            // Return JSON error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse(false, "An unexpected error occurred"));
        }
    }


    @RoleCheck
    @GetMapping("/invoice-reports")
    public ResponseEntity<Response<List<InvoiceResponse>>> getAllInvoices(HttpServletRequest request) {
        logger.info("Fetching all invoices");
        Response<List<InvoiceResponse>> response;
        String userRole = (String) request.getAttribute("userRole");
        if ("admin".equalsIgnoreCase(userRole)) {
            response = invoiceManager.getAllInvoices(); // all invoices
        } else if ("user".equalsIgnoreCase(userRole)) {
            response = invoiceManager.getDailyInvoices(); // e.g. daily only
        } else {
            response = new Response<>();
            response.setSuccess(false);
            response.setMessage("Unauthorized access");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        logger.info("Fetched all invoices response");
        return buildResponseEntity(response);
    }

    @GetMapping("/invoice/{id}")
    public ResponseEntity<Response<InvoiceResponse>> getInvoiceById(@PathVariable @Positive Long id) {
        logger.info("Fetching invoice with id: {}", id);
        Response<InvoiceResponse> response = invoiceManager.getInvoiceById(id);
        logger.info("Fetched invoice response {}",response);
        return buildResponseEntity(response);
    }

    @PutMapping("/invoice/{id}/update")
    public ResponseEntity<Response<InvoiceResponse>> updateInvoice(
            @PathVariable @Positive Long id,
            @Valid @RequestBody InvoiceRequest request) {
        logger.info("Updating invoice: {} with id: {}", request,id);
        Response<InvoiceResponse> response = invoiceManager.updateInvoice(id, request);
        logger.info("Updated invoice response {}",response);
        return buildResponseEntity(response);
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<Response<InvoiceResponse>> deleteInvoice(@PathVariable @Positive Long id) {
        logger.info("Deleting invoice with id: {}", id);
        Response<InvoiceResponse> response = invoiceManager.deleteInvoice(id);
        logger.info("Delete invoice response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/total-invoices")
    public ResponseEntity<Response<Long>> getTotalInvoices() {
        logger.info("Fetching total number of invoices");
        Response<Long> response = invoiceManager.getTotalInvoices();
        logger.info("Fetched total invoices response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/total-discounts")
    public ResponseEntity<Response<Long>> getTotalDiscounts() {
        logger.info("Fetching total number of discounts applied");
        Response<Long> response = invoiceManager.getTotalDiscounts();
        logger.info("Fetched total members having discounts response {}",response);
        return buildResponseEntity(response);
    }

}