package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.SmsTemplateRepository;
import com.jerrycode.gym_services.data.vo.SmsTemplate;
import com.jerrycode.gym_services.utils.Language;
import com.jerrycode.gym_services.utils.TemplateType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SmsTemplateInitializer implements CommandLineRunner {

    private final SmsTemplateRepository templateRepository;

    public SmsTemplateInitializer(SmsTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        // English templates
        createTemplateIfNotExists(TemplateType.PAYMENT_CONFIRMATION, Language.EN,
                "Dear {firstName}, thank you for your payment of {amount} for {package}. Your subscription is active from {startDate} to {endDate}. For support, call {supportPhone}.");
        createTemplateIfNotExists(TemplateType.SUBSCRIPTION_REMINDER, Language.EN,
                "Hi {firstName}, your {package} subscription ends on {endDate}. Renew now to stay active!");
        createTemplateIfNotExists(TemplateType.PAYMENT_REMINDER, Language.EN,
                "Dear {firstName}, you have a pending balance of {remainingAmount} TZS for {package}. Please complete payment.");
        createTemplateIfNotExists(TemplateType.MEMBER_WELCOME, Language.EN,
                "Dear {firstName}, welcome to {companyName}! We are excited to have you. Your fitness journey starts now. For any questions, contact us at {supportPhone}.");
        createTemplateIfNotExists(TemplateType.LOW_BALANCE_WARNING, Language.EN,
                "Dear {ownerName}, your SMS balance is low: {balance} credits remaining. Please top up to avoid service interruption.");

        // Swahili templates
        createTemplateIfNotExists(TemplateType.PAYMENT_CONFIRMATION, Language.SW,
                "Mpendwa {firstName}, asante kwa malipo yako ya {amount} kwa {package}. Usajili wako utakuwa aktif kutoka {startDate} mpaka {endDate}. Kwa msaada, piga {supportPhone}.");
        createTemplateIfNotExists(TemplateType.SUBSCRIPTION_REMINDER, Language.SW,
                "Habari {firstName}, usajili wako wa {package} utamalizika {endDate}. Sajili upya sasa ili kuendelea!");
        createTemplateIfNotExists(TemplateType.PAYMENT_REMINDER, Language.SW,
                "Mpendwa {firstName}, una deni la {remainingAmount} TZS kwa {package}. Tafadhali maliza malipo.");
        createTemplateIfNotExists(TemplateType.MEMBER_WELCOME, Language.SW,
                "Karibu {firstName} katika {companyName}! Tunafurahi kukuwa nasi. Safari yako ya mazoezi imeanza sasa. Kwa maswali yoyote, wasiliana nasi kupitia {supportPhone}.");
        createTemplateIfNotExists(TemplateType.LOW_BALANCE_WARNING, Language.SW,
                "Mpendwa {ownerName}, salio lako la SMS ni chini: {balance} tu zimesalia. Tafadhali ongeza salio ili kuepuka usumbufu wa huduma.");
    }

    private void createTemplateIfNotExists(TemplateType name, Language language, String message) {
        if (templateRepository.findByNameAndLanguage(name, language) == null) {
            SmsTemplate template = new SmsTemplate();
            template.setCreatedAt(new Date());
            template.setUpdatedAt(new Date());
            template.setName(name);
            template.setLanguage(language);
            template.setMessage(message);
            templateRepository.save(template);
        }
    }
}