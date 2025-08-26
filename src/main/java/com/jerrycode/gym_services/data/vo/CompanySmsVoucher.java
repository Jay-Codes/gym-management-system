package com.jerrycode.gym_services.data.vo;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "sms_vouchers")
public class CompanySmsVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String companyName;

    private String companyTinNumber;
    
    private double smsCount;  

    private double remainingSmsCount; 

    private String status;

    private String SmsPackageName;

    private String smsPackageProviderName;
    
    private LocalDateTime expiryDate;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "CompanySmsVoucher{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", companyTinNumber='" + companyTinNumber + '\'' +
                ", smsCount=" + smsCount +
                ", remainingSmsCount=" + remainingSmsCount +
                ", status='" + status + '\'' +
                ", SmsPackageName='" + SmsPackageName + '\'' +
                ", smsPackageProviderName=" + smsPackageProviderName +
                ", expiryDate=" + expiryDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
