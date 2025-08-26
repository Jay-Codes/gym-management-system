package com.jerrycode.gym_services.data.dto;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicesDTO {
    private Long id;
    private String userName;
    private String userPhone;
    private String userEmail;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private double amountPaid;
    private String status;
    private boolean paid;
    private String packageName;
    private String memo;
    private Double discountPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String invoiceFile;
    private String createdAt;
    private String updatedAt;
}
