package com.jerrycode.gym_services.utils;

import com.jerrycode.gym_services.business.service.FileStorageService;
import com.jerrycode.gym_services.data.vo.CompanyProfile;
import com.jerrycode.gym_services.data.vo.Invoices;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

@Component
public class PDFGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PDFGenerator.class);
    private final TemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;
    @Autowired
    private FileStorageService fileStorageService;
    private final String storagePath;

    public PDFGenerator(TemplateEngine templateEngine, ResourceLoader resourceLoader,
                        @Value("${file.upload-dir}") String storagePath) {
        this.templateEngine = templateEngine;
        this.resourceLoader = resourceLoader;
        this.storagePath = storagePath;
        logger.info("Initialized PDFGenerator with storagePath: {}", storagePath);
    }

    public byte[] generateInvoicePdf(Invoices invoice, CompanyProfile company) {
        try {
            long start = System.currentTimeMillis();
            logger.info("Starting PDF generation for invoice id: {}", invoice.getId());

            // Create a web context
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContextPath("");
            request.setServletPath("/api/invoice-report");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockServletContext servletContext = new MockServletContext();
            WebContext context = new WebContext(request, response, servletContext, new Locale("en"));
            context.setVariable("invoice", invoice);
            context.setVariable("company", company);

            // Handle logo as Base64
            String logoBase64 = "";
            if (company.getLogo() != null && !company.getLogo().isEmpty()) {
                logoBase64 = fileStorageService.getLogoBase64(company.getLogo());
                logger.debug("Generated logo Base64, length: {}", logoBase64.length());
            } else {
                logger.warn("No logo provided for company");
            }
            context.setVariable("logoBase64", logoBase64);

            // Render HTML with Thymeleaf
            logger.debug("Rendering invoice-template.html");
            String htmlContent = templateEngine.process("invoice-template", context);
            logger.debug("HTML content rendered, length: {} characters", htmlContent.length());

            // Generate PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            String baseUri = "file:///" + storagePath.replace("\\", "/") + "/";
            logger.debug("Setting base URI for PDF rendering: {}", baseUri);
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();
            outputStream.close();
            logger.info("PDF generated in {}ms for invoice id: {}", System.currentTimeMillis() - start, invoice.getId());

            return pdfBytes;
        } catch (Exception e) {
            logger.error("Failed to generate PDF for invoice id: {}: {}", invoice.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
}