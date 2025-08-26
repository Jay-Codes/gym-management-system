package com.jerrycode.gym_services.business.task;

import com.jerrycode.gym_services.business.service.SmsService;
import com.jerrycode.gym_services.utils.SmsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SmsScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SmsScheduler.class);

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private SmsService service;

    @Scheduled(cron = "${sms.scheduler.cron}") // every hour
    public void scheduledBalanceCheck() {
        double smsBalanceChecked = -1;
        try {
             smsBalanceChecked = smsClient.checkBalance();
             logger.info("Sms Balance Check Job Executed Current Balance {}",smsBalanceChecked);
        } catch (Exception e) {
            logger.warn("Scheduled balance check failed. Will use last known balance.");
        }
        if (smsBalanceChecked > 0 && smsBalanceChecked < 100) {
            service.sendLowBalanceWarning(smsBalanceChecked);
            logger.info("Sent Warning SMS to Company SMS Balance below 100");
        }
    }

}
