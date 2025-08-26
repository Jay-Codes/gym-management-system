package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.exception.ResourceNotFoundException;
import com.jerrycode.gym_services.request.InvoiceRequest;
import com.jerrycode.gym_services.response.InvoiceResponse;
import com.jerrycode.gym_services.response.Response;

import java.util.List;

public interface InvoiceManager {
    Response<InvoiceResponse> createInvoice(InvoiceRequest request);
    byte[] generateInvoiceReport(Long id) throws ResourceNotFoundException;
    Response<List<InvoiceResponse>> getAllInvoices();
    Response<List<InvoiceResponse>> getDailyInvoices();
    Response<InvoiceResponse> getInvoiceById(Long id);
    Response<InvoiceResponse> updateInvoice(Long id, InvoiceRequest request);
    Response<InvoiceResponse> deleteInvoice(Long id);
    Response<Long> getTotalInvoices();
    Response<Long> getTotalDiscounts();
}
