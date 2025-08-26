package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.SmsApiProviderRepository;
import com.jerrycode.gym_services.data.dao.SmsPackageRepository;
import com.jerrycode.gym_services.data.dao.CompanySmsVoucherRepository;
import com.jerrycode.gym_services.data.vo.SmsApiProvider;
import com.jerrycode.gym_services.data.vo.SmsPackage;
import com.jerrycode.gym_services.data.vo.CompanySmsVoucher;
import com.jerrycode.gym_services.utils.SMSProvider;
import com.jerrycode.gym_services.utils.Status;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
public class SmsDataInitializer {

    private final SmsPackageRepository smsPackageRepository;
    private final SmsApiProviderRepository smsApiProviderRepository;
    private final CompanySmsVoucherRepository smsVoucherRepository;

    public SmsDataInitializer(SmsPackageRepository smsPackageRepository,
                              SmsApiProviderRepository smsApiProviderRepository,
                              CompanySmsVoucherRepository smsVoucherRepository) {
        this.smsPackageRepository = smsPackageRepository;
        this.smsApiProviderRepository = smsApiProviderRepository;
        this.smsVoucherRepository = smsVoucherRepository;
    }

    @PostConstruct
    public void init() {
        initSmsPackages();
        initSmsApiProviders();
        initCompanySmsVouchers();
    }

    private void initSmsPackages() {
        if (smsPackageRepository.count() == 0) {
            SmsPackage basic = SmsPackage.builder()
                    .SmsPackageName("Basic Plan")
                    .smsCount(500.0)
                    .price(25.0)
                    .status(Status.ACTIVE)
                    .smsPackageProviderName(SMSProvider.BEEM_AFRICA)
                    .build();

            SmsPackage premium = SmsPackage.builder()
                    .SmsPackageName("Premium Plan")
                    .smsCount(2000.0)
                    .price(90.0)
                    .status(Status.ACTIVE)
                    .smsPackageProviderName(SMSProvider.BEEM_AFRICA)
                    .build();

            smsPackageRepository.save(basic);
            smsPackageRepository.save(premium);

            System.out.println("Default SMS packages initialized.");
        }
    }

    private void initSmsApiProviders() {
        if (smsApiProviderRepository.count() == 0) {
            SmsApiProvider provider = SmsApiProvider.builder()
                    .providerName(SMSProvider.BEEM_AFRICA)
                    .apiUrl("https://apisms.beem.africa/v1/send")
                    .apiBalanceUrl("https://apisms.beem.africa/public/v1/vendors/balance")
                    .senderId("4Js FITNESS")
                    .apiKey("80be5f816232bcfc")
                    .apiSecret("YzhmNDYyODYzYzY4YThiMmQ5NGE5OTMyOGMxYjM3ODJhODk4ZjMxZDBjYmY3ODQ4NzY0MjNkYmNmMmMzZTBjNA==")
                    .totalSmsCredits(10000)
                    .usedSmsCredits(0)
                    .active(true)
                    .build();

            smsApiProviderRepository.save(provider);

            System.out.println("Default SMS API provider initialized.");
        }
    }

    private void initCompanySmsVouchers() {
        if (smsVoucherRepository.count() == 0) {
            CompanySmsVoucher voucher = CompanySmsVoucher.builder()
                    .companyName("Fitness Center")
                    .companyTinNumber("123456789")
                    .smsCount(1000)
                    .remainingSmsCount(750)
                    .status("active")
                    .SmsPackageName("Basic Plan")
                    .expiryDate(LocalDateTime.now().plusMonths(1))
                    .build();

            smsVoucherRepository.save(voucher);

            System.out.println("Default Company SMS voucher initialized.");
        }
    }
}
