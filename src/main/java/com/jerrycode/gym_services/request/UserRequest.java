package com.jerrycode.gym_services.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String role;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String password;

    @JsonProperty("old_password")
    private String oldPassword;

    @Override
    public String toString() {
        return "UserRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='[PROTECTED]'" +
                ", oldPassword='[PROTECTED]'" +
                '}';
    }

}
