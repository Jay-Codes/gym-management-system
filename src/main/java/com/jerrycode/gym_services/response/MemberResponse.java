package com.jerrycode.gym_services.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jerrycode.gym_services.utils.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponse {
    private Long id;

    private String name;

    private String role;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String email;

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

}