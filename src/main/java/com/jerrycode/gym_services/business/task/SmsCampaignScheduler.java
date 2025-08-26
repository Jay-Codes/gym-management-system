//package com.jerrycode.gym_services.business.task;
//
//import com.jerrycode.gym_services.data.dao.CompanyProfileRepository;
//import com.jerrycode.gym_services.data.vo.CompanyProfile;
//import com.jerrycode.gym_services.data.vo.Invoices;
//import com.jerrycode.gym_services.data.vo.Member;
//import com.jerrycode.gym_services.data.dao.InvoiceRepository;
//import com.jerrycode.gym_services.data.dao.MemberRepository;
//import com.jerrycode.gym_services.business.service.SmsService;
//import com.jerrycode.gym_services.exception.ResourceNotFoundException;
//import com.jerrycode.gym_services.utils.TemplateType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class SmsCampaignScheduler {
//
//    private static final Logger logger = LoggerFactory.getLogger(SmsCampaignScheduler.class);
//    private final InvoiceRepository invoicesRepository;
//    private final MemberRepository memberRepository;
//    private final SmsService smsService;
//    private final CompanyProfileRepository companyProfileRepository;
//
//    public SmsCampaignScheduler(InvoiceRepository invoicesRepository, MemberRepository memberRepository,
//                                SmsService smsService, CompanyProfileRepository companyProfileRepository) {
//        this.invoicesRepository = invoicesRepository;
//        this.memberRepository = memberRepository;
//        this.smsService = smsService;
//        this.companyProfileRepository = companyProfileRepository;
//    }
//
//    // Check for new invoices every 5 minutes
//    @Scheduled(cron = "")
//    public void checkNewPayments() {
//        logger.info("Checking for new invoices");
//        List<Invoices> newInvoices = invoicesRepository.findByStatusAndCreatedAtAfter("PAID", LocalDate.now().minusDays(1).atStartOfDay());
//        // Add company info to placeholders
//        CompanyProfile company = companyProfileRepository.findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
//        for (Invoices invoice : newInvoices) {
//            Optional<Member> memberOpt = memberRepository.findById(invoice.getMemberId());
//            if (!memberOpt.isPresent()) {
//                logger.warn("Member not found for invoice ID {}", invoice.getId());
//                continue;
//            }
//            Member member = memberOpt.get();
//            String packageName = invoice.getPackageName();
//            Map<String, String> placeholders = new HashMap<>();
//            placeholders.put("firstName", member.getName());
//            placeholders.put("amount", String.format("%.2f", invoice.getAmountPaid()));
//            placeholders.put("package", packageName);
//            placeholders.put("companyName", company.getCompanyName());
//            placeholders.put("supportPhone", company.getPhone());
//
//            smsService.sendSmsAsync(member, TemplateType.PAYMENT_CONFIRMATION, placeholders);
//        }
//        logger.info("Processed {} new payment SMS", newInvoices.size());
//    }
//
//    // Check for expiring subscriptions daily at 8 AM
//    @Scheduled(cron = "")
//    public void checkExpiringSubscriptions() {
//        logger.info("Checking for expiring subscriptions");
//        LocalDate expiryThreshold = LocalDate.now().plusDays(3);
//        List<Invoices> expiringInvoices = invoicesRepository.findByEndDateBeforeAndStatusNot(expiryThreshold, "PAID");
//        CompanyProfile company = companyProfileRepository.findFirst()
//                .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
//        for (Invoices invoice : expiringInvoices) {
//            Optional<Member> memberOpt = memberRepository.findById(invoice.getMemberId());
//            if (!memberOpt.isPresent()) {
//                logger.warn("Member not found for invoice ID {}", invoice.getId());
//                continue;
//            }
//            Member member = memberOpt.get();
//            String packageName = invoice.getPackageName();
//            Map<String, String> placeholders = new HashMap<>();
//            placeholders.put("firstName", member.getName());
//            placeholders.put("package", packageName);
//            placeholders.put("endDate", invoice.getEndDate() != null
//                    ? invoice.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
//                    : "unknown");
//            placeholders.put("companyName", company.getCompanyName());
//            placeholders.put("supportPhone", company.getPhone());
//
//            smsService.sendSmsAsync(member, TemplateType.SUBSCRIPTION_REMINDER, placeholders);
//        }
//        logger.info("Processed {} expiry reminder SMS", expiringInvoices.size());
//    }
//
//    // Check for partial payments daily at 9 AM
//    @Scheduled(cron = "")
//    public void checkPartialPayments() {
//        logger.info("Checking for partial payments");
//        List<Invoices> partialInvoices = invoicesRepository.findByStatus("PARTIAL");
//        for (Invoices invoice : partialInvoices) {
//            Optional<Member> memberOpt = memberRepository.findById(invoice.getMemberId());
//            if (!memberOpt.isPresent()) {
//                logger.warn("Member not found for invoice ID {}", invoice.getId());
//                continue;
//            }
//            Member member = memberOpt.get();
//            String packageName = invoice.getPackageName();
//            // Assuming totalAmount is not directly available; adjust if you have a total field
//            double remainingAmount = invoice.getAmountPaid() > 0 ? invoice.getAmountPaid() * (1 - invoice.getDiscountPercentage() / 100) : 0;
//            Map<String, String> placeholders = new HashMap<>();
//            placeholders.put("firstName", member.getName());
//            placeholders.put("remainingAmount", String.format("%.2f", remainingAmount));
//            placeholders.put("package", packageName);
//
//            smsService.sendSmsAsync(member, TemplateType.SUBSCRIPTION_REMINDER, placeholders);
//        }
//        logger.info("Processed {} partial payment reminder SMS", partialInvoices.size());
//    }
//}