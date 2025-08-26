package com.jerrycode.gym_services.data.vo;

import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User information
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_phone", nullable = false)
    private String userPhone;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(name = "member_phone", nullable = false)
    private String memberPhone;

    // Financial fields
    @Column(name = "amount_paid", nullable = false, precision = 20, scale = 2)
    private double amountPaid;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "paid", nullable = false)
    private boolean paid = true;

    // Package and discount details
    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "memo")
    private String memo;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private double discountPercentage;

    // Date fields
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "invoice_file")
    private String invoiceFile;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-persist and pre-update hooks for timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Invoices{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberPhone='" + memberPhone + '\'' +
                ", amountPaid=" + amountPaid +
                ", status='" + status + '\'' +
                ", paid=" + paid +
                ", packageName='" + packageName + '\'' +
                ", discountPercentage=" + discountPercentage +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", invoiceFile='" + invoiceFile + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}