package com.jerrycode.gym_services.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerrycode.gym_services.data.dao.SmsApiProviderRepository;
import com.jerrycode.gym_services.data.vo.SmsApiProvider;
import com.jerrycode.gym_services.response.SmsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class SmsClient {

    private static final Logger logger = LoggerFactory.getLogger(SmsClient.class);
    private final RestTemplate restTemplate;
    private final String smsApiUrl;
    private final String apiKey;
    private final String apiSecret;
    private final String senderId;
    private final String smsBalanceUrl;
    private final SmsApiProviderRepository smsApiProviderRepository;


    public SmsClient(RestTemplate restTemplate,
                     @Value("${easysms.api.url}") String smsApiUrl,
                     @Value("${easysms.api.balance.url}") String smsBalanceUrl,
                     @Value("${easysms.api.key}") String apiKey,
                     @Value("${easysms.api.secret}") String apiSecret,
                     @Value("${easysms.api.senderid}") String senderId, SmsApiProviderRepository smsApiProviderRepository) {
        this.restTemplate = restTemplate;
        this.smsApiUrl = smsApiUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.senderId = senderId;
        this.smsBalanceUrl = smsBalanceUrl;
        this.smsApiProviderRepository = smsApiProviderRepository;
    }

    public SmsResponse sendSms(String phoneNumber, String message) {
        try {
            // Prepare headers with Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", getBasicAuthHeader());

            // Prepare request body according to Beem API spec
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("source_addr", senderId);
            requestBody.put("encoding", 0); // 0 for plain text
            requestBody.put("message", message);

            // Prepare recipients
            Map<String, Object> recipient = new HashMap<>();
            recipient.put("recipient_id", 1); // Can be any number, just needs to be unique in the list
            recipient.put("dest_addr", formatPhoneNumber(phoneNumber));

            requestBody.put("recipients", Collections.singletonList(recipient));

            logger.debug("Sending SMS to {} with message: {}", phoneNumber, message);

            ResponseEntity<Map> response = restTemplate.exchange(
                    smsApiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            logger.info("SMS Callback Response {}",response);

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                SmsResponse smsResponse = SmsResponse.fromApiResponse(responseBody);

                if (smsResponse.isStatus()) {
                    logger.info("SMS sent successfully to {}. Request ID: {}", phoneNumber, smsResponse.getRequestId());
                } else {
                    logger.warn("SMS failed to send to {}. Code: {}, Message: {}",
                            phoneNumber, smsResponse.getCode(), smsResponse.getMessage());
                }
                return smsResponse;
            } else {
                logger.warn("Empty response from SMS provider for phone {}", phoneNumber);
                return new SmsResponse(false, "Empty response from SMS provider",
                        null, null, null, null, null, null);
            }

        } catch (HttpClientErrorException e) {
            logger.error("SMS API error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return parseErrorResponse(e);
        } catch (Exception e) {
            logger.error("SMS sending failed to {}", phoneNumber, e);
            return new SmsResponse(false, "SMS service unavailable: " + e.getMessage(),
                    null, null, null, null, null, null);
        }
    }

    public double checkBalance() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", getBasicAuthHeader());

            logger.debug("Checking SMS credit balance from: {}", smsBalanceUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    smsBalanceUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data").path("credit_balance");

            if (dataNode.isMissingNode() || !dataNode.isNumber()) {
                throw new RuntimeException("Invalid balance response from Beem");
            }

            double balance = dataNode.asDouble();
            logger.info("Fetched SMS credit balance: {}", balance);

            // Fetch and update the provider record
            SmsApiProvider provider = smsApiProviderRepository.findTopByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            provider.setTotalSmsCredits(balance);
            provider.setUpdatedAt(LocalDateTime.now());
            smsApiProviderRepository.save(provider);

            return balance;

        } catch (Exception e) {
            logger.error("Failed to check SMS balance", e);
            throw new RuntimeException("Failed to check SMS balance", e);
        }
    }

    private String getBasicAuthHeader() {
        String auth = apiKey + ":" + apiSecret;
        byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove any non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        // If number starts with 0, replace with country code (255 for Tanzania)
        if (digitsOnly.startsWith("0")) {
            return "255" + digitsOnly.substring(1);
        }

        // If number starts with +, just remove the +
        if (digitsOnly.startsWith("+")) {
            return digitsOnly.substring(1);
        }

        // Otherwise return as is (assuming it's already in international format)
        return digitsOnly;
    }

    private SmsResponse parseErrorResponse(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> errorDetails = mapper.readValue(responseBody, Map.class);

            // Try to extract error information
            String errorMessage = "SMS failed";
            Integer errorCode = null;

            if (errorDetails.containsKey("message")) {
                errorMessage = String.valueOf(errorDetails.get("message"));
            }
            if (errorDetails.containsKey("code")) {
                errorCode = Integer.valueOf(errorDetails.get("code").toString());
            }

            return new SmsResponse(
                    false,
                    errorMessage,
                    errorCode,
                    null,  // requestId
                    null,  // valid
                    null,  // invalid
                    null,  // duplicates
                    errorDetails
            );
        } catch (Exception ex) {
            return new SmsResponse(
                    false,
                    "SMS failed: " + e.getStatusCode() + " - " + e.getStatusText(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    Collections.singletonMap("raw_error", e.getResponseBodyAsString())
            );
        }
    }
}