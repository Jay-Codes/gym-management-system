package com.jerrycode.gym_services.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Schema(description = "user login object", example =
    "{\n" +
    "  \"email\": \"john@example.com\",\n" +
    "  \"password\": \"+255123456789\"\n" +
    "}")
@Data
public class LoginRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
