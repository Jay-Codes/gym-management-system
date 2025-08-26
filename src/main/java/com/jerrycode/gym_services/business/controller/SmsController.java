package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.SmsService;
import com.jerrycode.gym_services.data.dao.MemberRepository;
import com.jerrycode.gym_services.data.vo.Member;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.utils.TemplateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);
    private final SmsService smsService;
    private final MemberRepository memberRepository;

    public SmsController(SmsService smsService, MemberRepository memberRepository) {
        this.smsService = smsService;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/send/{memberId}/{templateName}")
    public Response<String> sendSms(@PathVariable Long memberId,
                                    @PathVariable TemplateType templateName,
                                    @RequestBody Map<String, String> placeholders) {
        Member member = null;
        logger.info("Received request to send SMS to member {} with template {} having placeholders of {}", memberId, templateName,placeholders);
        Response<String> response = new Response<>();
        try {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

            logger.info("Sending SMS for member {}",member);

            CompletableFuture<Boolean> resultFuture = smsService.sendSmsAsync(member, templateName, placeholders);
            Boolean result = resultFuture.get();
            if(result){
                response.setSuccess(true);
                response.setMessage("SMS sent to member " + memberId);
                logger.info("SMS sent to member {}", member);
            } else {
                response.setSuccess(false);
                response.setMessage("SMS failed to send to member " + memberId);
                logger.info("SMS failed to send to member {}", member);
            }

        } catch (IllegalArgumentException e) {
            logger.warn("Failed to send SMS: {}", e.getMessage(),e);
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending SMS to member {}: {}", memberId, e);
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Received response {} to send SMS to member {}",response,member);
        return response;
    }
}