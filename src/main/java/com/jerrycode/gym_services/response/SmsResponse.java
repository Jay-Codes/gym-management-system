package com.jerrycode.gym_services.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {
    private boolean status;
    private String message;
    private Integer code;
    private Integer requestId;
    private Integer valid;
    private Integer invalid;
    private Integer duplicates;
    private Map<String, Object> details;

    // Helper method to create response from API map
    public static SmsResponse fromApiResponse(Map<String, Object> apiResponse) {
        boolean success = Boolean.TRUE.equals(apiResponse.get("successful"));
        String msg = (String) apiResponse.get("message");
        Integer code = (Integer) apiResponse.get("code");
        Integer requestId = (Integer) apiResponse.get("request_id");
        Integer valid = (Integer) apiResponse.get("valid");
        Integer invalid = (Integer) apiResponse.get("invalid");
        Integer duplicates = (Integer) apiResponse.get("duplicates");

        return new SmsResponse(
                success,
                msg,
                code,
                requestId,
                valid,
                invalid,
                duplicates,
                apiResponse
        );
    }
}