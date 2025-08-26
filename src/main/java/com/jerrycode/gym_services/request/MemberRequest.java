package com.jerrycode.gym_services.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jerrycode.gym_services.utils.Language;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
public class MemberRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String role;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @Email
    private String email;

    @NotBlank
    private String gender;

    private String height;

    private String weight;

    private String memo;

    @Enumerated(EnumType.STRING)
    private Language preferredLanguage;

    private String packageSubscribed;

    private LocalDateTime subscribedStartDate;

    private LocalDateTime subscribedEndDate;

    private double packagePaidAmount;

    @Override
    public String toString() {
        return "MemberRequest{" +
                "name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", height='" + height + '\'' +
                ", weight='" + weight + '\'' +
                ", memo='" + memo + '\'' +
                ", preferredLanguage=" + preferredLanguage +
                ", packageSubscribed='" + packageSubscribed + '\'' +
                ", subscribedStartDate=" + subscribedStartDate +
                ", subscribedEndDate=" + subscribedEndDate +
                ", packagePaidAmount=" + packagePaidAmount +
                '}';
    }


}