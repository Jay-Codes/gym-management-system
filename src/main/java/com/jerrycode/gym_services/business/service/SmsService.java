package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.*;
import com.jerrycode.gym_services.data.vo.Packages;
import com.jerrycode.gym_services.data.vo.*;
import com.jerrycode.gym_services.response.SmsResponse;
import com.jerrycode.gym_services.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private final SmsClient smsClient;
    private final SmsTemplateRepository templateRepository;
    private final SmsCampaignRepository campaignRepository;
    private final CompanySmsVoucherRepository smsVoucherRepository;
    private final NotificationRepository notificationRepository;
    private final SmsApiProviderRepository smsApiProviderRepository;
    private final CompanySmsCampaignRepository companySmsCampaignRepository;
    private final CompanyNotificationRepository companyNotificationRepository;
    private final CompanyProfileRepository companyProfileRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    //private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    public SmsService(SmsClient smsClient, SmsTemplateRepository templateRepository,
                      SmsCampaignRepository campaignRepository, CompanySmsVoucherRepository smsVoucherRepository, NotificationRepository notificationRepository, SmsApiProviderRepository smsApiProviderRepository, CompanySmsCampaignRepository companySmsCampaignRepository, CompanyNotificationRepository companyNotificationRepository, CompanyProfileRepository companyProfileRepository) {
        this.smsClient = smsClient;
        this.templateRepository = templateRepository;
        this.campaignRepository = campaignRepository;
        this.smsVoucherRepository = smsVoucherRepository;
        this.notificationRepository = notificationRepository;
        this.smsApiProviderRepository = smsApiProviderRepository;
        this.companySmsCampaignRepository = companySmsCampaignRepository;
        this.companyNotificationRepository = companyNotificationRepository;
        this.companyProfileRepository = companyProfileRepository;
    }

    @Async("smsTaskExecutor")
    @Transactional
    public CompletableFuture<Boolean> sendSmsAsync(Member member, TemplateType templateName, Map<String, String> placeholders) {
        boolean sentSms = false;
        logger.info("Preparing SMS for member {} using template {} having placeholders {}",
                member != null ? member.getId() : "null", templateName, placeholders);
        if (member == null || templateName == null) {
            logger.error("Invalid input: member or templateName is null");
            throw new IllegalArgumentException("Member and template name cannot be null");
        }

        SmsCampaign campaign = null;
        try {
            // Get member's preferred language (default to English if not set)
            Language language = member.getPreferredLanguage() != null ?
                    member.getPreferredLanguage() : Language.EN;

            SmsTemplate template = templateRepository.findByNameAndLanguage(templateName, language);
            if (template == null) {
                // Fallback to English if template not found in member's language
                logger.warn("Template not found for language {}, falling back to English", language);
                template = templateRepository.findByNameAndLanguage(templateName, Language.EN);
                if (template == null) {
                    logger.warn("Template not found: {}", templateName);
                    throw new IllegalArgumentException("Template not found: " + templateName);
                }
            }

            String message = renderTemplate(template.getMessage(), placeholders);
            campaign = createCampaign(member, template);

            SmsResponse response = smsClient.sendSms(member.getPhoneNumber(), message);
            Notification notification = createNotification(campaign, member, message, response,template);

            logger.info("SMS Response {}",response);

            // Update campaign status based on response
            campaign.setExecutedAt(LocalDateTime.now());
            campaign.setExecuted(true);
            campaign.setStatus(response.isStatus() ? CampaignStatus.SENT : CampaignStatus.FAILED);
            // Record all response details
            if (response.isStatus()) {
                sentSms = true;
                campaign.setDescription(String.format("SMS sent successfully. Request ID: %d",
                        response.getRequestId()));
            } else {
                campaign.setErrorMessage(String.format("%s (Error Code: %d)",
                        response.getMessage(),
                        response.getCode()));

                // Store additional details as JSON in description
                campaign.setDescription(String.format("Failed to send SMS. Details: %s",
                        response.getDetails() != null ? response.getDetails().toString() : "No details"));
            }

            // Save the external reference (request ID)
            if (response.getRequestId() != null) {
                campaign.setName("SMS-" + response.getRequestId());
            }

            campaignRepository.save(campaign);
            notificationRepository.save(notification);

            logger.info("SMS {} for member {} (ID: {}). Request ID: {}. Message: {}",
                    response.isStatus() ? "sent successfully" : "failed to send",
                    member.getName(),
                    member.getId(),
                    response.getRequestId(),
                    response.getMessage());

        } catch (IllegalArgumentException e) {
            logger.error("Error sending SMS to member {}: {}", member.getId(), e);
            if (campaign != null) {
                campaign.setStatus(CampaignStatus.FAILED);
                campaign.setErrorMessage(e.getMessage());
                campaign.setDescription("Failed before sending: " + e.getMessage());
                campaignRepository.save(campaign);
            }
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error sending SMS to member {}: {}", member.getId(), e);
            if (campaign != null) {
                campaign.setStatus(CampaignStatus.FAILED);
                campaign.setErrorMessage("Unexpected error: " + e.getMessage());
                campaign.setDescription("Failed with exception: " + e);
                campaignRepository.save(campaign);
            }
            throw new RuntimeException("Failed to send SMS", e);
        }
        return CompletableFuture.completedFuture(sentSms);
    }

    @Async("smsCompanyTaskExecutor")
    @Transactional
    public CompletableFuture<Boolean> sendSmsCompanyAsync(CompanyProfile companyProfile, TemplateType templateName, Map<String, String> placeholders) {
        boolean sentSms;
        CompanySmsCampaign campaign = null;

        if (companyProfile == null || templateName == null) {
            logger.error("Invalid input: companyProfile or templateName is null");
            throw new IllegalArgumentException("Company and template name cannot be null");
        }

        try {
            Language language = companyProfile.getPreferredLanguage() != null ? companyProfile.getPreferredLanguage() : Language.EN;
            SmsTemplate template = templateRepository.findByNameAndLanguage(templateName, language);

            if (template == null) {
                logger.warn("Template not found in {}, falling back to EN", language);
                template = templateRepository.findByNameAndLanguage(templateName, Language.EN);
                if (template == null) throw new IllegalArgumentException("Template not found for " + templateName);
            }

            String message = renderTemplate(template.getMessage(), placeholders);
            campaign = createCompanyCampaign(companyProfile, template);

            SmsResponse response = smsClient.sendSms(companyProfile.getPhone(), message);
            CompanyNotification notification = createCompanyNotification(campaign, companyProfile, message, response,template);

            campaign.setExecutedAt(LocalDateTime.now());
            campaign.setExecuted(true);
            campaign.setStatus(response.isStatus() ? CampaignStatus.SENT : CampaignStatus.FAILED);
            campaign.setDescription(response.isStatus()
                    ? "SMS sent successfully. Request ID: " + response.getRequestId()
                    : String.format("Failed to send SMS. Error: %s (Code: %d)", response.getMessage(), response.getCode()));

            if (response.getRequestId() != null) {
                campaign.setName("SMS-" + response.getRequestId());
            }

            companySmsCampaignRepository.save(campaign);
            companyNotificationRepository.save(notification);

            sentSms = response.isStatus();
            logger.info("SMS {} for company {}. Request ID: {}. Message: {}", sentSms ? "sent" : "failed", companyProfile.getId(), response.getRequestId(), message);

        } catch (Exception e) {
            logger.error("Failed to send SMS to company {}: {}", companyProfile.getId(), e.getMessage());
            if (campaign != null) {
                campaign.setStatus(CampaignStatus.FAILED);
                campaign.setErrorMessage(e.getMessage());
                campaign.setDescription("Failed with exception: " + e);
                companySmsCampaignRepository.save(campaign);
            }
            throw new RuntimeException("Failed to send SMS", e);
        }

        return CompletableFuture.completedFuture(sentSms);
    }

    private String renderTemplate(String template, Map<String, String> placeholders) {
        logger.info("Rendering Template {} with placeholders {} ",template,placeholders);
        String message = template;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}",
                        entry.getValue() != null ? entry.getValue() : "");
            }
        }
        logger.info("Returning compiled message response back {}",message);
        return message;
    }

    private SmsCampaign createCampaign(Member member, SmsTemplate template) {
        logger.info("Campaign for member {} with template {}",member,template);
        SmsCampaign campaign = new SmsCampaign();
        campaign.setTemplate(template != null ? template : new SmsTemplate());
        campaign.setMemberId(member);
        campaign.setStatus(CampaignStatus.PENDING);
        campaign.setScheduledAt(LocalDateTime.now());
        campaign.setExecuted(false);
        return campaignRepository.save(campaign);
    }

    private Notification createNotification(SmsCampaign campaign, Member member, String message, SmsResponse response, SmsTemplate smsTemplate) {
        logger.info("Notification {} to member {} with message {} on campaign {}",response,member,message,campaign);
        Notification notification = new Notification();
        notification.setCampaign(campaign);
        notification.setMemberId(member);
        notification.setPhoneNumber(member.getPhoneNumber());
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notification.setTemplate(smsTemplate);
        notification.setStatus(response.isStatus() ? Status.SENT : Status.FAILED);
        notification.setErrorMessage(response.isStatus() ? null : response.getMessage());
        return notification;
    }

    private CompanySmsCampaign createCompanyCampaign(CompanyProfile company, SmsTemplate template) {
        CompanySmsCampaign campaign = new CompanySmsCampaign();
        campaign.setTemplate(template);
        campaign.setCompany(company);
        campaign.setStatus(CampaignStatus.PENDING);
        campaign.setScheduledAt(LocalDateTime.now());
        campaign.setExecuted(false);
        return campaign;
    }

    private CompanyNotification createCompanyNotification(CompanySmsCampaign campaign, CompanyProfile company, String message, SmsResponse response, SmsTemplate smsTemplate) {
        CompanyNotification notification = new CompanyNotification();
        notification.setCampaign(campaign);
        notification.setCompany(company);
        notification.setPhoneNumber(company.getPhone());
        notification.setMessage(message);
        notification.setTemplate(smsTemplate);
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus(response.isStatus() ? Status.SENT : Status.FAILED);
        notification.setErrorMessage(response.isStatus() ? null : response.getMessage());
        return notification;
    }


    public void sendWelcomeSms(Member member, CompanyProfile company) {
        logger.info("Sending welcome SMS to {}", member);
        try {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("firstName", member.getName());
            placeholders.put("companyName", company.getCompanyName());
            placeholders.put("supportPhone", company.getPhone());

            sendProviderTemplatedSms(member, TemplateType.MEMBER_WELCOME, placeholders);

            logger.info("Welcome SMS sent for member {}", member);
        } catch (Exception e) {
            logger.error("Failed to send welcome SMS to member {}: {}", member.getId(), e.getMessage());
        }
    }


    public void sendPaymentConfirmationSms(Member member, Invoices invoice, Packages pkg, CompanyProfile company) {
        logger.info("Sending payment confirmation SMS to member {}", member);
        try {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("firstName", member.getName());
            placeholders.put("companyName", company.getCompanyName());
            placeholders.put("package", pkg.getName());
            placeholders.put("amount", String.format("%.2f", invoice.getAmountPaid()));
            placeholders.put("startDate", invoice.getStartDate().format(DATE_FORMATTER));
            placeholders.put("endDate", invoice.getEndDate().format(DATE_FORMATTER));
            placeholders.put("supportPhone", company.getPhone());

            sendProviderTemplatedSms(member, TemplateType.PAYMENT_CONFIRMATION, placeholders);

            logger.info("Payment confirmation SMS sent for invoice {}", invoice.getId());
        } catch (Exception e) {
            logger.error("Failed to send payment confirmation SMS for invoice {}: {}", invoice.getId(), e.getMessage());
        }
    }

    public void sendLowBalanceWarning(double balance) {
        try {
            CompanyProfile company = companyProfileRepository.findTopByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Company not found"));


            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("ownerName", company.getCompanyName());
            placeholders.put("balance", String.format("%.2f", balance - 1));

            TemplateType templateType = TemplateType.LOW_BALANCE_WARNING;

            sendCompanyProviderTemplatedSms(company, templateType, placeholders);
        } catch (Exception e) {
            logger.error("Failed to send low balance SMS warning: {}", e.getMessage());
        }

    }

    private void sendTemplatedSms(Member member, CompanyProfile company, TemplateType templateType,
                                  Map<String, String> placeholders) throws Exception {
        CompanySmsVoucher voucher = smsVoucherRepository.findByCompanyName(company.getCompanyName())
                .orElseThrow(() -> new RuntimeException("No SMS voucher found for company"));

        SmsApiProvider smsApiProvider = smsApiProviderRepository.findByProviderName(voucher.getSmsPackageProviderName())
                .orElseThrow(() -> new RuntimeException("No SMS provider found for company"));

        CompletableFuture<Boolean> resultFuture = sendSmsAsync(member, templateType, placeholders);
        boolean smsSent = resultFuture.get(); // blocking

        if (smsSent) {
            voucher.setRemainingSmsCount(voucher.getRemainingSmsCount() - 1);
            smsApiProvider.setTotalSmsCredits(smsApiProvider.getTotalSmsCredits() - 1);
            smsApiProvider.setUsedSmsCredits(smsApiProvider.getUsedSmsCredits() + 1);

            smsVoucherRepository.save(voucher);
            smsApiProviderRepository.save(smsApiProvider);
            logger.info("SMS sent successfully. Remaining voucher: {}", voucher);
        } else {
            logger.error("Failed to send {} SMS for company {}", templateType, company);
            throw new RuntimeException("Failed to send SMS");
        }
    }

    private void sendProviderTemplatedSms(Member member, TemplateType templateType,
                                  Map<String, String> placeholders) throws Exception {

        SmsApiProvider smsApiProvider = smsApiProviderRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("No SMS provider found"));

        CompletableFuture<Boolean> resultFuture = sendSmsAsync(member, templateType, placeholders);
        boolean smsSent = resultFuture.get(); // blocking

        if (smsSent) {
            smsApiProvider.setUsedSmsCredits(smsApiProvider.getUsedSmsCredits() + 1);
            smsApiProvider.setTotalSmsCredits(smsApiProvider.getTotalSmsCredits() - 1);
            
            smsApiProviderRepository.save(smsApiProvider);
            logger.info("SMS sent successfully. Remaining sms Credits from Provider: {}", smsApiProvider);
        } else {
            logger.error("Failed to send {} SMS from Provider {}", templateType, smsApiProvider);
            throw new RuntimeException("Failed to send SMS");
        }
    }

    private void sendCompanyProviderTemplatedSms(CompanyProfile companyProfile, TemplateType templateType,
                                          Map<String, String> placeholders) throws Exception {

        SmsApiProvider smsApiProvider = smsApiProviderRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("No SMS provider found"));

        CompletableFuture<Boolean> resultFuture = sendSmsCompanyAsync(companyProfile, templateType, placeholders);
        boolean smsSent = resultFuture.get(); // blocking

        if (smsSent) {
            smsApiProvider.setTotalSmsCredits(smsApiProvider.getTotalSmsCredits() - 1);
            smsApiProvider.setUsedSmsCredits(smsApiProvider.getUsedSmsCredits() + 1);

            smsApiProviderRepository.save(smsApiProvider);
            logger.info("SMS sent successfully. Remaining sms Credits from Provider: {}", smsApiProvider);
        } else {
            logger.error("Failed to send {} SMS from Provider {}", templateType, smsApiProvider);
            throw new RuntimeException("Failed to send SMS");
        }
    }


    public void refillSmsVoucher(CompanyProfile company, SmsPackage smsPackage) {

        CompanySmsVoucher voucher = smsVoucherRepository.findByCompanyName(company.getCompanyName())
            .orElse(new CompanySmsVoucher());

        voucher.setCompanyName(company.getCompanyName());
        voucher.setCompanyTinNumber(company.getTin());
        voucher.setSmsCount(smsPackage.getSmsCount());
        voucher.setRemainingSmsCount(voucher.getRemainingSmsCount()+smsPackage.getSmsCount());
        voucher.setSmsPackageName(smsPackage.getSmsPackageName());
        voucher.setExpiryDate(company.getCompanySubscriptionEndDate());

        smsVoucherRepository.save(voucher);
        logger.info("Refilled SMS voucher for company {}", company);
    }


        public boolean checkSmsBalance(CompanyProfile company) {
            logger.info("Checking SMS balance for company {}", company);

            try {
                CompanySmsVoucher voucher = smsVoucherRepository.findByCompanyName(company.getCompanyName())
                        .orElseThrow(() -> new RuntimeException("No SMS voucher found for company: " + company.getCompanyName()));

                boolean isLowBalance = voucher.getRemainingSmsCount() < (voucher.getSmsCount() * 0.1);
                boolean isExpired = voucher.getExpiryDate().isBefore(LocalDateTime.now());

                if (isLowBalance) {
                    logger.warn("Company {} has low SMS balance: {}/{}",
                            company.getCompanyName(), voucher.getRemainingSmsCount(), voucher.getSmsCount());
                    return false;
                }
                if (isExpired) {
                    logger.warn("Company {} subscription has expired", company);
                    return false;
                }

                return true;

            } catch (Exception e) {
                logger.error("Error checking SMS balance for company {}: {}", company, e.getMessage(), e);
                return false;
            }
        }

    public boolean checkProviderSmsBalance() {
        SmsApiProvider smsApiProvider = null;
        logger.info("Checking Provider SMS balance");
        try {
            smsApiProvider = smsApiProviderRepository.findTopByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("No SMS API provider found"));

            boolean isLowBalance = smsApiProvider.getTotalSmsCredits() < 100;

            if (isLowBalance) {
                logger.warn("Currently having low SMS balance for provider {}",smsApiProvider);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("Error checking SMS balance for Provider {}", smsApiProvider, e);
            return false;
        }
    }

}
