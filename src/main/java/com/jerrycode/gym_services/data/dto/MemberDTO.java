package com.jerrycode.gym_services.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private Long id;
    private String name;
    private String role;
    private String phoneNumber;
    private String email;
    private String gender;
    private String height;
    private String weight;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
