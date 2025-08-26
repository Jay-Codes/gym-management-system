package com.jerrycode.gym_services.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BuildResponseEntity {

    private static final Logger logger = LoggerFactory.getLogger(BuildResponseEntity.class);

    public static <T> ResponseEntity<Response<T>> buildResponseEntity(Response<T> response) {
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        // Determine HTTP status based on error message or code
        String message = response.getMessage().toLowerCase();
        if (message.contains("not found")) {
            logger.error("404 NOT FOUND: {}", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (message.contains("invalid") || message.contains("incorrect")) {
            logger.warn("400 BAD REQUEST: {}", message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else if (message.contains("unauthorized") || message.contains("authentication failed")) {
            logger.warn("401 UNAUTHORIZED: {}", message);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (message.contains("forbidden") || message.contains("access denied")) {
            logger.error("403 FORBIDDEN: {} - Access denied", message);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } else {
            // Default to 400 for generic errors
            logger.warn("400 BAD REQUEST (General Error): {}", message);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }
}
