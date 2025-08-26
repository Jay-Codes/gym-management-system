package com.jerrycode.gym_services.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @JsonProperty("old_password")
    private String oldPassword;
    @JsonProperty("password")
    private String newPassword;

    @Override
    public String toString() {
        return "PasswordUpdateRequest{" +
                "oldPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }

}
